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

public class StringLinkedNode<V> implements Node<String, V> {

    private int size;
    private LinkedNodeItem root;

    @Override
    public int size() {
        return size;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        int hashCode = key.hashCode();
        LinkedNodeItem node = getNodeByIndex(this.root, hashCode, 0);
        return node == null ? null : node.getValue();
    }

    @Override
    public V put(String key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        int hashCode = key.hashCode();
        if (this.root == null) this.root = new LinkedNodeItem();
        this.size++;
        return add(this.root, hashCode, value, 0);
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        int hashCode = key.hashCode();
        LinkedNodeItem currentNode = getNodeByIndex(this.root, hashCode, 0);
        if (currentNode == null) return null;
        LinkedNodeItem previous = currentNode.getPrevious();
        LinkedNodeItem next = currentNode.getNext();
        if (previous == null) {
            if (currentNode == this.root) this.root = next;
        } else if (next == null) {
            if (currentNode == this.root) this.root = previous;
        } else {
            previous.setNext(next);
            next.setPrevious(previous);
            if (currentNode == this.root) this.root = previous;
        }
        this.size--;
        return currentNode.getValue();
    }

    private V add(LinkedNodeItem node, int index, V value, int state) {
        if (node.getIndex() > index) {
            LinkedNodeItem previous = node.getPrevious();
            if (previous != null) {
                if (state != -1) {
                    return add(previous, index, value, 1);
                } else {
                    LinkedNodeItem newNode = new LinkedNodeItem();
                    newNode.setPrevious(previous);
                    newNode.setIndex(index);
                    newNode.setValue(value);
                    newNode.setNext(node);
                    node.setPrevious(newNode);
                }
            } else {
                previous = new LinkedNodeItem();
                previous.setIndex(index);
                previous.setValue(value);
                previous.setNext(node);
                node.setPrevious(previous);
            }
        } else if (node.getIndex() < index) {
            LinkedNodeItem next = node.getNext();
            if (next != null) {
                if (state != 1) {
                    return add(next, index, value, -1);
                } else {
                    LinkedNodeItem newNode = new LinkedNodeItem();
                    newNode.setPrevious(node);
                    newNode.setIndex(index);
                    newNode.setValue(value);
                    newNode.setNext(next);
                    node.setNext(newNode);
                }
            } else {
                next = new LinkedNodeItem();
                next.setIndex(index);
                next.setValue(value);
                next.setPrevious(node);
                node.setNext(next);
            }
        } else {
            V result = node.getValue();
            node.setValue(value);
            return result;
        }
        return null;
    }

    private LinkedNodeItem getNodeByIndex(LinkedNodeItem node, int index, int state) {
        if (node.getIndex() > index) {
            LinkedNodeItem previous = node.getPrevious();
            if (previous != null) {
                if (state != -1) return getNodeByIndex(previous, index, 1);
            }
            return null;
        } else if (node.getIndex() < index) {
            LinkedNodeItem next = node.getNext();
            if (next != null) {
                if (state != 1) return getNodeByIndex(next, index, -1);
            }
            return null;
        }
        return node;
    }

    public class LinkedNodeItem {
        private V value;
        private int index;
        private LinkedNodeItem next;
        private LinkedNodeItem previous;

        public LinkedNodeItem getPrevious() {
            return previous;
        }

        public void setPrevious(LinkedNodeItem previous) {
            this.previous = previous;
        }

        public LinkedNodeItem getNext() {
            return next;
        }

        public void setNext(LinkedNodeItem next) {
            this.next = next;
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
    }
}
