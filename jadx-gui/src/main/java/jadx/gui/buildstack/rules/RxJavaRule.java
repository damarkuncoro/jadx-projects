package jadx.gui.buildstack.rules;

public class RxJavaRule extends BaseLibraryAndClassRule {
    public RxJavaRule() {
        super(
            "RxJava",
            "HIGH",
            "io.reactivex",
            "io/reactivex"
        );
    }
}
