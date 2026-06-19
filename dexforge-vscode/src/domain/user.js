class User {
    constructor(id, name) {
        this.id = id;
        this.name = name;
    }

    static fromDto(dto) {
        return new User(dto.id, dto.name);
    }
}

module.exports = User;
