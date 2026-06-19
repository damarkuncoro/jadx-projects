package dexforge.api.plugins.input.data;

import java.util.List;

import dexforge.api.plugins.input.data.annotations.EncodedValue;
import dexforge.api.plugins.input.insns.custom.ICustomPayload;

public interface ICallSite extends ICustomPayload {

	List<EncodedValue> getValues();

	void load();
}
