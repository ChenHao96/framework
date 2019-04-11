/**
 * Copyright 2019 ChenHao96
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.steven.chen.model.game;

public abstract class TablePlayerModel extends PlayerModel {

    private TableModel table;

    public TablePlayerModel(int playerId, String playerName) {
        super(playerId, playerName);
    }

    public TablePlayerModel(int playerId, String playerName, TableModel table) {
        super(playerId, playerName);
        this.table = table;
    }

    public TableModel getTable() {
        return table;
    }

    public void setTable(TableModel table) {
        this.table = table;
    }
}
