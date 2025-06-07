package com.cutec.collect.parse;

import org.jsoup.nodes.Document;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParseCore<T> {
    public void parse(Supplier<Document> getDocument,
                      Function<Document, T> parseDocument,
                      Consumer<T> mediaConsumer) {
        Document document = getDocument.get();
        T t = parseDocument.apply(document);
        mediaConsumer.accept(t);
    }

    public T parse(Supplier<Document> getDocument,
                   Function<Document, T> parseDocument) {
        Document document = getDocument.get();
        return parseDocument.apply(document);
    }
}
