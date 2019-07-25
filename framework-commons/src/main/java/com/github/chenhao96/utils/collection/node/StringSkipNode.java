/**
 * Copyright 2017-2019 ChenHao96
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
package com.github.chenhao96.utils.collection.node;

import java.util.Map;

public class StringSkipNode<V> implements Node<String, V> {

    private int size;
    private NodeItem root;

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.root = null;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashKey(key.hashCode());
        return node == null ? null : node.currentNode == null ? null : node.currentNode.value;
    }

    @Override
    public V put(String key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");
        int keyHash = key.hashCode();
        NodeItem current = new NodeItem();
        if (initRoot(current)) return null;
        this.size++;
        current.key = key;
        current.value = value;
        current.index = keyHash;
        return putLevelNode(current);
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashKey(key.hashCode());
        if (node != null) {
            this.size--;
            if (node.currentNode.levelNext == null) {
                if (node.previousNode == null) {
                    if (node.previousLevel == null) {
                        this.root = node.currentNode.dataNext;
                    } else {
                        node.previousLevel.levelNext = node.currentNode.dataNext;
                    }
                } else {
                    node.previousNode.dataNext = node.currentNode.dataNext;
                }
            } else {
                //TODO:
                if (node.previousNode == null) {
                } else {
                }
            }
            return node.currentNode.value;
        }
        return null;
    }

    private boolean initRoot(NodeItem valueNode) {
        if (this.root == null) {
            this.root = valueNode;
            this.size = 1;
            return true;
        }
        return false;
    }

    private NodeItem findLessCurrent(int hash, NodeItem current) {
        while (true) {
            if (hash < current.index) return null;
            if (hash == current.index) return current;
            if (hash > current.index) {
                if (current.dataNext == null) return current;
                if (hash > current.dataNext.index) {
                    current = current.dataNext;
                } else if (hash == current.dataNext.index) {
                    return current.dataNext;
                } else {
                    return current;
                }
            }
        }
    }

    private V putLevelNode(NodeItem value) {
        V result = null;
        NodeItem current = this.root, root = this.root, previousLevel = null;
        while (true) {
            NodeItem tmp = findLessCurrent(value.index, current);
            if (tmp != null) {
                if (tmp.index == value.index) {
                    result = tmp.value;
                    tmp.value = value.value;
                    break;
                } else {
                    if (tmp.dataNext != null) {
                        if (tmp.levelNext != null) {
                            previousLevel = tmp;
                            current = tmp.levelNext;
                        } else {
                            tmp.levelNext = value;
                            break;
                        }
                    } else {
                        tmp.dataNext = value;
                        break;
                    }
                }
            } else {
                value.dataNext = current;
                if (previousLevel == null) {
                    root = value;
                } else {
                    previousLevel.levelNext = value;
                }
                break;
            }
        }
        this.root = root;
        return result;
    }

    private ResultNode queryHashKey(int hashCode) {
        NodeItem previousLevel = null, previous = null, current = this.root;
        while (current != null && hashCode >= current.index) {
            if (hashCode == current.index) return new ResultNode(previousLevel, previous, current);
            NodeItem nextData = current.dataNext;
            if (nextData == null || hashCode < nextData.index) {
                previous = null;
                previousLevel = current;
                current = current.levelNext;
            } else {
                //TODO:
                if (nextData.dataNext == null) break;
                if (hashCode < nextData.dataNext.index) {
                    previous = null;
                    previousLevel = nextData;
                    current = nextData.levelNext;
                } else if (hashCode > nextData.dataNext.index) {
                    previous = nextData.dataNext;
                    current = nextData.dataNext.dataNext;
                } else {
                    return new ResultNode(previousLevel, nextData, nextData.dataNext);
                }
            }
        }
        return null;
    }

    private class ResultNode {
        private NodeItem previousLevel;
        private NodeItem previousNode;
        private NodeItem currentNode;

        public ResultNode(NodeItem previousLevel, NodeItem previousNode, NodeItem currentNode) {
            this.previousLevel = previousLevel;
            this.previousNode = previousNode;
            this.currentNode = currentNode;
        }
    }

    public class NodeItem implements Map.Entry<String, V> {

        public V value;
        public int index;
        public String key;
        public NodeItem dataNext;
        public NodeItem levelNext;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V result = this.value;
            this.value = value;
            return result;
        }
    }
}
