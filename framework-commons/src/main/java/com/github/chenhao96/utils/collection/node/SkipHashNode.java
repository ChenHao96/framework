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

public class SkipHashNode<K, V> implements Node<K, V> {

    private int size;
    private NodeItem root;

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
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
    public V put(K key, V value) {

        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");

        int keyHash = key.hashCode();
        if (initRoot(key, value, keyHash)) return null;

        this.size++;
        NodeItem current = new NodeItem();
        current.index = keyHash;
        current.value = value;
        current.key = key;
        return putLevelNode(current);
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashKey(key.hashCode());
        if (node != null) {
            this.size--;
            //TODO:bug
            NodeItem tmpNode;
            NodeItem currentNextLevel = node.currentNode.levelNext;
            if (currentNextLevel != null) {
                tmpNode = currentNextLevel;
                while (tmpNode.dataNext != null) {
                    tmpNode = tmpNode.dataNext;
                }
                tmpNode.dataNext = node.currentNode.dataNext;
                if (node.previousNode != null) {
                    node.previousNode.dataNext = currentNextLevel;
                } else if (node.previousLevel != null) {
                    node.previousLevel.levelNext = currentNextLevel;
                } else {
                    this.root = currentNextLevel;
                }
            } else {
                if (node.previousNode != null) {
                    node.previousNode.dataNext = node.currentNode.dataNext;
                } else if (node.previousLevel != null) {
                    node.previousLevel.levelNext = node.currentNode.dataNext;
                } else {
                    this.root = node.currentNode.dataNext;
                }
            }
            return node.currentNode.value;
        }
        return null;
    }

    private boolean initRoot(K key, V value, int keyHash) {
        if (this.root == null) {
            this.root = new NodeItem();
            this.root.index = keyHash;
            this.root.value = value;
            this.root.key = key;
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
                previous = current;
                current = nextData;
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

    public class NodeItem  {
        private K key;
        private V value;
        private int index;
        private NodeItem dataNext;
        private NodeItem levelNext;

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public NodeItem getDataNext() {
            return dataNext;
        }

        public void setDataNext(NodeItem dataNext) {
            this.dataNext = dataNext;
        }

        public NodeItem getLevelNext() {
            return levelNext;
        }

        public void setLevelNext(NodeItem levelNext) {
            this.levelNext = levelNext;
        }
    }
}
