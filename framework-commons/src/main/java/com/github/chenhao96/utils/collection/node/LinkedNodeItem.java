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

public class LinkedNodeItem<D> extends AbstractNodeItem<D> {

    private LinkedNodeItem<D> previous;

    private LinkedNodeItem<D> next;

    public LinkedNodeItem<D> getPrevious() {
        return previous;
    }

    public void setPrevious(LinkedNodeItem<D> previous) {
        this.previous = previous;
    }

    public LinkedNodeItem<D> getNext() {
        return next;
    }

    public void setNext(LinkedNodeItem<D> next) {
        this.next = next;
    }
}