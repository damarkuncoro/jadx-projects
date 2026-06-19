const { Readable, Writable } = require('stream');

class LspStreamBridge {
    static createReaderStream(proc, logError) {
        const readable = new Readable({
            read() {}
        });

        let stdoutBuffer = '';
        proc.stdout.on('data', (chunk) => {
            stdoutBuffer += chunk.toString('utf8');
            let lineIndex;
            while ((lineIndex = stdoutBuffer.indexOf('\n')) !== -1) {
                const line = stdoutBuffer.substring(0, lineIndex).trim();
                stdoutBuffer = stdoutBuffer.substring(lineIndex + 1);
                if (line.length > 0) {
                    try {
                        const obj = JSON.parse(line);
                        obj.jsonrpc = '2.0';
                        if (obj.status === 'ERROR' && obj.error) {
                            if (typeof obj.error === 'string') {
                                obj.error = { code: -32000, message: obj.error };
                            } else if (typeof obj.error === 'object' && !obj.error.message) {
                                obj.error.message = JSON.stringify(obj.error);
                            }
                        }
                        const modifiedLine = JSON.stringify(obj);
                        const payload = Buffer.from(modifiedLine, 'utf8');
                        const header = `Content-Length: ${payload.length}\r\n\r\n`;
                        const lspMessage = Buffer.concat([Buffer.from(header, 'utf8'), payload]);
                        readable.push(lspMessage);
                    } catch (e) {
                        if (logError) {
                            logError(`Failed to parse daemon output line: ${line}. Error: ${e.message}`);
                        }
                    }
                }
            }
        });

        proc.stdout.on('end', () => {
            if (stdoutBuffer.trim().length > 0) {
                const line = stdoutBuffer.trim();
                try {
                    const obj = JSON.parse(line);
                    obj.jsonrpc = '2.0';
                    if (obj.status === 'ERROR' && obj.error) {
                        if (typeof obj.error === 'string') {
                            obj.error = { code: -32000, message: obj.error };
                        } else if (typeof obj.error === 'object' && !obj.error.message) {
                            obj.error.message = JSON.stringify(obj.error);
                        }
                    }
                    const modifiedLine = JSON.stringify(obj);
                    const payload = Buffer.from(modifiedLine, 'utf8');
                    const header = `Content-Length: ${payload.length}\r\n\r\n`;
                    const lspMessage = Buffer.concat([Buffer.from(header, 'utf8'), payload]);
                    readable.push(lspMessage);
                } catch (e) {
                    // ignore
                }
            }
            readable.push(null);
        });

        return readable;
    }

    static createWriterStream(proc, logError) {
        let writeBuffer = Buffer.alloc(0);
        const writable = new Writable({
            write(chunk, encoding, callback) {
                writeBuffer = Buffer.concat([writeBuffer, chunk]);
                try {
                    while (true) {
                        const str = writeBuffer.toString('utf8');
                        const headerEnd = str.indexOf('\r\n\r\n');
                        if (headerEnd === -1) {
                            break;
                        }
                        const headerPart = str.substring(0, headerEnd);
                        const contentLengthMatch = headerPart.match(/Content-Length:\s*(\d+)/i);
                        if (!contentLengthMatch) {
                            writeBuffer = Buffer.alloc(0);
                            break;
                        }
                        const contentLength = parseInt(contentLengthMatch[1], 10);
                        const headerByteLength = Buffer.byteLength(headerPart, 'utf8') + 4;
                        if (writeBuffer.length < headerByteLength + contentLength) {
                            break;
                        }
                        const bodyBuffer = writeBuffer.subarray(headerByteLength, headerByteLength + contentLength);
                        const bodyStr = bodyBuffer.toString('utf8');

                        proc.stdin.write(bodyStr + '\n');

                        writeBuffer = writeBuffer.subarray(headerByteLength + contentLength);
                    }
                } catch (e) {
                    if (logError) {
                        logError(`Failed to parse LSP chunk: ${e.message}`);
                    }
                }
                callback();
            }
        });

        writable.on('finish', () => {
            proc.stdin.end();
        });

        return writable;
    }
}

module.exports = LspStreamBridge;
