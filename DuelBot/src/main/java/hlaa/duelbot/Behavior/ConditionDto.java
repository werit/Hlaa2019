/*
 * Copyright (C) 2019 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
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
package hlaa.duelbot.Behavior;

/**
 *
 * @author msi
 */
public class ConditionDto {

    public ConditionDto(boolean isEnemyInFocus, boolean isEnemyInFocusAtLocation, boolean isNotNavigating, boolean isHealthBelow70) {
        this.isEnemyInFocus = isEnemyInFocus;
        this.isEnemyInFocusAtLocation = isEnemyInFocusAtLocation;
        this.isNotNavigating = isNotNavigating;
        this.isHealthBelow70 = isHealthBelow70;
    }
    public boolean isEnemyInFocus;
    public boolean isEnemyInFocusAtLocation;
    public boolean isNotNavigating;
    public boolean isHealthBelow70;
    
    private boolean[] GetConditionsAsAnArray() {
        return new boolean[]{isEnemyInFocus, isEnemyInFocusAtLocation, isNotNavigating,isHealthBelow70};
    }

    public boolean AreConditionsMetAtCurrentRun(ConditionDto conditionsCurrentlyMet) {
        boolean[] requiredConditionsArray = this.GetConditionsAsAnArray();
        boolean[] conditionsMetArray = conditionsCurrentlyMet.GetConditionsAsAnArray();
        for (int i = 0; i < requiredConditionsArray.length; i++) {
            if (requiredConditionsArray[i] && conditionsMetArray[i] != requiredConditionsArray[i]) {
                return false;
            }
        }
        return true;
    }
}
