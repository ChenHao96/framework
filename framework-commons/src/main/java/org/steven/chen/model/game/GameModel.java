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

public abstract class GameModel<T extends RoomModel> {

    private int gameId;
    private Set<T> rooms;
    private String gameName;
    private ServerModel server;

    public GameModel() {
    }

    public GameModel(int gameId, String gameName) {
        this.gameId = gameId;
        this.gameName = gameName;
    }

    public GameModel(int gameId, String gameName, Set<T> rooms) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.rooms = rooms;
    }

    public int getGameId() {
        return gameId;
    }

    public ServerModel getServer() {
        return server;
    }

    public void setServer(ServerModel server) {
        this.server = server;
    }

    public String getGameName() {
        return gameName;
    }

    public Set<T> getAllRoom() {
        return rooms;
    }

    public void addRoom(T room) {
        if (room == null) return;
        if (rooms == null) rooms = new HashSet<>();
        rooms.add(room);
    }

    @SafeVarargs
    public final void addRooms(T... rooms) {
        if (rooms != null && rooms.length > 0) {
            for (T room : rooms) {
                addRoom(room);
            }
        }
    }

    public T getRoomByName(String name) {
        if (!StringUtil.isNotEmpty(name)) return null;
        if (rooms == null) return null;
        for (T room : rooms) {
            if (room == null) continue;
            if (name.equals(room.getRoomName())) {
                return room;
            }
        }
        return null;
    }

    public void clear() {
        if (rooms == null) return;
        rooms.clear();
    }

    public T remove(String name) {
        if (rooms == null) return null;
        T result = getRoomByName(name);
        if (result != null) {
            rooms.remove(result);
        }
        return result;
    }
}
