package me.kmathers.sudobot.quill.mocks;

public class MockPotionEffect {
    private final String type;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    
    public MockPotionEffect(String type, int duration, int amplifier) {
        this(type, duration, amplifier, true, true);
    }
    
    public MockPotionEffect(String type, int duration, int amplifier, boolean ambient, boolean particles) {
        this.type = type.toLowerCase();
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
    }
    
    public String getType() {
        return type;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public int getAmplifier() {
        return amplifier;
    }
    
    public boolean isAmbient() {
        return ambient;
    }
    
    public boolean hasParticles() {
        return particles;
    }
    
    public String getDisplayName() {
        return formatEffectName(type);
    }
    
    private String formatEffectName(String type) {
        StringBuilder sb = new StringBuilder();
        String[] parts = type.split("_");
        for (String part : parts) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayName());
        
        if (amplifier > 0) {
            sb.append(" ").append(toRoman(amplifier + 1));
        }
        
        sb.append(" (").append(formatDuration(duration)).append(")");
        
        return sb.toString();
    }
    
    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + "m";
            } else {
                return minutes + "m " + remainingSeconds + "s";
            }
        }
    }
    
    private String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(number);
        }
    }
}