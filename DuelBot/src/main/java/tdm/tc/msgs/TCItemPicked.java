package tdm.tc.msgs;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;

public class TCItemPicked extends TCMessageData {
	
	private static final long serialVersionUID = 78663234234912377L;

	public static final IToken MESSAGE_TYPE = Tokens.get("TCItemPicked");
	
	private UnrealId who;
	
	private UnrealId what;	
	
	public TCItemPicked(UnrealId who, UnrealId what) {
		super(MESSAGE_TYPE);
		this.who = who;
		this.what = what;
	}

	public UnrealId getWho() {
		return who;
	}

	public void setWho(UnrealId who) {
		this.who = who;
	}

	public UnrealId getWhat() {
		return what;
	}

	public void setWhat(UnrealId what) {
		this.what = what;
	}
	
}
