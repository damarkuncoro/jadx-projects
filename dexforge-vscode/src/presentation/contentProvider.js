const vscode = require('vscode');

class DecompilerContentProvider {
    constructor() {
        this.onDidChangeEmitter = new vscode.EventEmitter();
        this.onDidChange = this.onDidChangeEmitter.event;
        this.documents = new Map();
    }
    
    provideTextDocumentContent(uri) {
        return this.documents.get(uri.toString()) || '';
    }
}

const decompilerProvider = new DecompilerContentProvider();

module.exports = {
    DecompilerContentProvider,
    decompilerProvider
};
