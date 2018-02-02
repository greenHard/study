package com.zhang.study.chapter.part2.seven.test;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 自定义Spliterator
 */
public class WordCounterSpliterator implements Spliterator<String>{

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        return false;
    }

    @Override
    public Spliterator<String> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}
