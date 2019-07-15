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
    private LinkedNodeItem<V> root;

    @Override
    public int size() {
        return size;
    }

    @Override
    public V get(String key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        int hashCode = key.hashCode();
        LinkedNodeItem<V> node = getNodeByIndex(this.root, hashCode, 0);
        return node == null ? null : node.getValue();
    }

    @Override
    public V put(String key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        int hashCode = key.hashCode();
        if (this.root == null) this.root = new LinkedNodeItem<>();
        add(this.root, hashCode, value, 0);
        this.size++;
        return value;
    }

    @Override
    public V remove(String key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        int hashCode = key.hashCode();
        LinkedNodeItem<V> currentNode = getNodeByIndex(this.root, hashCode, 0);
        if (currentNode == null) return null;

        LinkedNodeItem<V> previous = currentNode.getPrevious();
        LinkedNodeItem<V> next = currentNode.getNext();
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

    private void add(LinkedNodeItem<V> node, int index, V value, int state) {
        if (node.getIndex() > index) {
            LinkedNodeItem<V> previous = node.getPrevious();
            if (previous != null) {
                if (state != -1) {
                    add(previous, index, value, 1);
                } else {
                    LinkedNodeItem<V> newNode = new LinkedNodeItem<>();
                    newNode.setPrevious(previous);
                    newNode.setIndex(index);
                    newNode.setValue(value);
                    newNode.setNext(node);
                    node.setPrevious(newNode);
                }
            } else {
                previous = new LinkedNodeItem<>();
                previous.setIndex(index);
                previous.setValue(value);
                previous.setNext(node);
                node.setPrevious(previous);
            }
        } else if (node.getIndex() < index) {
            LinkedNodeItem<V> next = node.getNext();
            if (next != null) {
                if (state != 1) {
                    add(next, index, value, -1);
                } else {
                    LinkedNodeItem<V> newNode = new LinkedNodeItem<>();
                    newNode.setPrevious(node);
                    newNode.setIndex(index);
                    newNode.setValue(value);
                    newNode.setNext(next);
                    node.setNext(newNode);
                }
            } else {
                next = new LinkedNodeItem<>();
                next.setIndex(index);
                next.setValue(value);
                next.setPrevious(node);
                node.setNext(next);
            }
        } else {
            node.setValue(value);
        }
    }

    private LinkedNodeItem<V> getNodeByIndex(LinkedNodeItem<V> node, int index, int state) {
        if (node.getIndex() > index) {
            LinkedNodeItem<V> previous = node.getPrevious();
            if (previous != null) {
                if (state != -1) return getNodeByIndex(previous, index, 1);
            }
            return null;
        } else if (node.getIndex() < index) {
            LinkedNodeItem<V> next = node.getNext();
            if (next != null) {
                if (state != 1) return getNodeByIndex(next, index, -1);
            }
            return null;
        }
        return node;
    }
}
