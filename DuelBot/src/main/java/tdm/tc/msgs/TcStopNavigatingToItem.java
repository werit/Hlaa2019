/*
 * Copyright (C) 2020 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tdm.tc.msgs;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.teamcomm.mina.messages.TCMessageData;
import cz.cuni.amis.utils.token.IToken;
import cz.cuni.amis.utils.token.Tokens;

/**
 *
 * @author msi
 */
public class TcStopNavigatingToItem extends TCMessageData {
    private static final long serialVersionUID = 786632342349123812L;

    public static final IToken MESSAGE_TYPE = Tokens.get("TcStopNavigatingToItem");
    
    private UnrealId who;
    
    private UnrealId what;
        public TcStopNavigatingToItem(UnrealId who, UnrealId what) {
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
