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

import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public abstract class TableModel<T extends TablePlayerModel> {

    private int tableId;
    private String tableName;
    private Set<T> tablePlayers;

    public TableModel(int tableId, String tableName) {
        this.tableId = tableId;
        this.tableName = tableName;
    }

    public TableModel(int tableId, String tableName, Set<T> tablePlayers) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.tablePlayers = tablePlayers;
    }

    public int getTableId() {
        return tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public Set<T> getAllTablePlayer() {
        return tablePlayers;
    }

    public void addTablePlayer(T player) {
        if (player == null) return;
        if (tablePlayers == null) tablePlayers = new HashSet<>();
        tablePlayers.add(player);
    }

    public void addTablePlayers(T... players) {
        if (players != null && players.length > 0) {
            for (T player : players) {
                addTablePlayer(player);
            }
        }
    }

    public T getTablePlayByName(String name) {
        if (!StringUtils.hasLength(name)) return null;
        if (tablePlayers == null) return null;
        for (T player : tablePlayers) {
            if (player == null) continue;
            if (name.equals(player.getPlayerName())) {
                return player;
            }
        }
        return null;
    }

    public void clear() {
        if (tablePlayers == null) return;
        tablePlayers.clear();
    }

    public T remove(String name) {
        if (tablePlayers == null) return null;
        T result = getTablePlayByName(name);
        if (result != null) {
            tablePlayers.remove(result);
        }
        return result;
    }
}
