const cp = require('child_process');
const Device = require('../domain/device');
const User = require('../domain/user');
const Package = require('../domain/package');

class DeviceExplorerService {
    constructor(dexforgeBinary) {
        this.dexforgeBinary = dexforgeBinary;
    }

    listDevices() {
        return new Promise((resolve, reject) => {
            cp.exec(`${this.dexforgeBinary} device-explorer list-devices --format json`, (error, stdout, stderr) => {
                if (error) {
                    return reject(new Error(stderr || error.message));
                }

                try {
                    const result = JSON.parse(stdout);
                    if (result && result.status === 'ERROR') {
                        return reject(new Error(result.error.message || JSON.stringify(result.error)));
                    }

                    const devicesDto = Array.isArray(result) ? result : (result.result || []);
                    const devices = devicesDto.map(dto => Device.fromDto(dto));
                    resolve(devices);
                } catch (e) {
                    reject(new Error(`Failed to parse daemon output: ${e.message}`));
                }
            });
        });
    }

    listUsers(serial) {
        return new Promise((resolve, reject) => {
            cp.exec(`${this.dexforgeBinary} device-explorer list-users ${serial} --format json`, (error, stdout, stderr) => {
                if (error) {
                    return reject(new Error(stderr || error.message));
                }

                try {
                    const result = JSON.parse(stdout);
                    if (result && result.status === 'ERROR') {
                        return reject(new Error(result.error.message || JSON.stringify(result.error)));
                    }

                    const usersDto = Array.isArray(result) ? result : (result.result || []);
                    const users = usersDto.map(dto => User.fromDto(dto));
                    resolve(users);
                } catch (e) {
                    reject(new Error(`Failed to parse users output: ${e.message}`));
                }
            });
        });
    }

    listPackages(serial, userId) {
        return new Promise((resolve, reject) => {
            cp.exec(`${this.dexforgeBinary} device-explorer list-packages ${serial} ${userId} user --format json`, (error, stdout, stderr) => {
                if (error) {
                    return reject(new Error(stderr || error.message));
                }

                try {
                    const result = JSON.parse(stdout);
                    if (result && result.status === 'ERROR') {
                        return reject(new Error(result.error.message || JSON.stringify(result.error)));
                    }

                    const packagesDto = Array.isArray(result) ? result : (result.result || []);
                    const packages = packagesDto.map(dto => Package.fromDto(dto));
                    resolve(packages);
                } catch (e) {
                    reject(new Error(`Failed to parse packages output: ${e.message}`));
                }
            });
        });
    }

    pullAndDecompile(serial, packageName, outDir, userId) {
        return new Promise((resolve, reject) => {
            cp.exec(`${this.dexforgeBinary} device-explorer pull-and-decompile ${serial} ${packageName} "${outDir}" ${userId} --format json`, (error, stdout, stderr) => {
                if (error) {
                    return reject(new Error(stderr || error.message));
                }

                try {
                    const result = JSON.parse(stdout);
                    if (result && result.status === 'ERROR') {
                        return reject(new Error(result.error.message || JSON.stringify(result.error)));
                    }

                    resolve(result); // returns PullResultDto
                } catch (e) {
                    reject(new Error(`Failed to parse pull-and-decompile output: ${e.message}`));
                }
            });
        });
    }
}

module.exports = DeviceExplorerService;
