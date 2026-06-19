class Package {
    constructor(packageName, appName, userId, type, path) {
        this.packageName = packageName;
        this.appName = appName;
        this.userId = userId;
        this.type = type;
        this.path = path;
    }

    static fromDto(dto) {
        return new Package(
            dto.packageName,
            dto.appName,
            dto.userId,
            dto.type,
            dto.path
        );
    }
}

module.exports = Package;
