package cr.chromapie.bpopener.core;

public enum OpenAction {

    USE(false),
    SNEAK_USE(true);

    private final boolean sneaking;

    OpenAction(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public boolean isSneaking() {
        return sneaking;
    }
}
