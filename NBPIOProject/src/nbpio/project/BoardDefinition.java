package nbpio.project;

public final class BoardDefinition {

    private final String name;
    private final String type;
    private final String MCU;
    private final String frequency;
    private final String flash;
    private final String RAM;

    private BoardDefinition(String name, String type, String MCU, String frequency, String flash, String RAM) {
        this.name = name;
        this.type = type;
        this.MCU = MCU;
        this.frequency = frequency;
        this.flash = flash;
        this.RAM = RAM;
    }

    public String getName() {
        return name;
    }

    public String getFlash() {
        return flash;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getMCU() {
        return MCU;
    }

    public String getRAM() {
        return RAM;
    }

    public String getType() {
        return type;
    }
    
    public static class Builder {
        
        private String name;
        private String type;
        private String MCU;
        private String frequency;
        private String flash;
        private String RAM;
        
        public Builder name( String name ) {
            this.name = name;
            return this;
        }
        
        public Builder type( String type ) {
            this.type = type;
            return this;
        }
        
        public Builder MCU( String MCU ) {
            this.MCU = MCU;
            return this;
        }
        
        public Builder frequency( String frequency ) {
            this.frequency = frequency;
            return this;
        }
        
        public Builder flash( String flash ) {
            this.flash = flash;
            return this;
        }
        
        public Builder RAM( String RAM ) {
            this.RAM = RAM;
            return this;
        }
        
        public BoardDefinition build() {
            return new BoardDefinition(name, type, MCU, frequency, flash, RAM);
        }
        
    }
    
}
