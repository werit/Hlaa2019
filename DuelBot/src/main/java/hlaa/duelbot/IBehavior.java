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

package hlaa.duelbot;

/**
 *
 * @author msi
 */
public interface IBehavior {
    // TODO what about priority
    /*protected ConditionDto RequiredConditions;
    private double Priority;*/
    
    /*public IBehavior(double priority, ConditionDto requiredConditions)throws NullPointerException{
        this.Priority = priority;
        if (requiredConditions == null){
            throw new NullPointerException("Required conditions cannot be null");
        }
        this.RequiredConditions = requiredConditions;
    }*/
    public BotCapabilities[] GetBotCapabilities();
    public IBehavior Stop();
    public IBehavior Execute();
    
    public boolean IsUsable(ConditionDto conditionDto);/*{
        return RequiredConditions.AreConditionsMet(conditionDto);
    }  */
    public double GetPriority();/*{
        return this.Priority;
    }*/
}
