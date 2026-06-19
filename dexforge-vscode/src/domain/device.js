class Device {
    constructor(serial, state, model) {
        this.serial = serial;
        this.state = state;
        this.model = model || 'Unknown';
    }

    static fromDto(dto) {
        return new Device(
            dto.serial,
            dto.state,
            dto.model
        );
    }
}

module.exports = Device;
