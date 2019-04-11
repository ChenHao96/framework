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

public abstract class RoomModel<T extends PlayerModel> {

    private int roomId;
    private GameModel game;
    private Set<T> players;
    private String roomName;

    public RoomModel(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public RoomModel(int roomId, String roomName, Set<T> players) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.players = players;
    }

    public int getRoomId() {
        return roomId;
    }

    public GameModel getGame() {
        return game;
    }

    public void setGame(GameModel game) {
        this.game = game;
    }

    public String getRoomName() {
        return roomName;
    }

    public Set<T> getAllPlayer() {
        return players;
    }

    public void addPlayer(T player) {
        if (player == null) return;
        if (players == null) players = new HashSet<>();
        players.add(player);
    }

    public void addPlayers(T... players) {
        if (players != null && players.length > 0) {
            for (T player : players) {
                addPlayer(player);
            }
        }
    }

    public T getPlayerByName(String name) {
        if (!StringUtils.hasLength(name)) return null;
        if (players == null) return null;
        for (T player : players) {
            if (player == null) continue;
            if (name.equals(player.getPlayerName())) {
                return player;
            }
        }
        return null;
    }

    public void clear() {
        if (players == null) return;
        players.clear();
    }

    public T remove(String name) {
        if (players == null) return null;
        T result = getPlayerByName(name);
        if (result != null) {
            players.remove(result);
        }
        return result;
    }
}
