package jadx.gui.buildstack.rules;

public class RetrofitRule extends BaseLibraryAndClassRule {
    public RetrofitRule() {
        super(
            "Retrofit",
            "HIGH",
            "com.squareup.retrofit",
            "retrofit2"
        );
    }
}
