class ClassMetadata {
    constructor(fullName, name, alias, packageName) {
        this.fullName = fullName;
        this.name = name;
        this.alias = alias;
        this.package = packageName;
    }

    static fromDto(dto) {
        return new ClassMetadata(
            dto.fullName,
            dto.name,
            dto.alias,
            dto.package
        );
    }
}

module.exports = ClassMetadata;
