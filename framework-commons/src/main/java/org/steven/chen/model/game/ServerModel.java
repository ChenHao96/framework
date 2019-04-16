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

import org.steven.chen.utils.StringUtil;

import java.util.HashSet;
import java.util.Set;

public abstract class ServerModel<T extends GameModel> {

    private int serverId;
    private Set<T> games;
    private String serverName;

    public ServerModel(int serverId, String serverName) {
        this.serverId = serverId;
        this.serverName = serverName;
    }

    public ServerModel(int serverId, String serverName, Set<T> games) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.games = games;
    }

    public int getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public Set<T> getAllGame() {
        return games;
    }

    public void addGame(T game) {
        if (game == null) return;
        if (games == null) games = new HashSet<>();
        games.add(game);
    }

    @SafeVarargs
    public final void addGames(T... games) {
        if (games != null && games.length > 0) {
            for (T game : games) {
                addGame(game);
            }
        }
    }

    public T getGameByName(String name) {
        if (!StringUtil.isNotEmpty(name)) return null;
        if (games == null) return null;
        for (T game : games) {
            if (game == null) continue;
            if (name.equals(game.getGameName())) {
                return game;
            }
        }
        return null;
    }

    public void clear() {
        if (games == null) return;
        games.clear();
    }

    public T remove(String name) {
        if (games == null) return null;
        T result = getGameByName(name);
        if (result != null) {
            games.remove(result);
        }
        return result;
    }
}
