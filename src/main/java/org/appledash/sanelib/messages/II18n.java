package org.appledash.sanelib.messages;

/**
 * Created by appledash on 7/6/17.
 * Blackjack is best pony.
 *
 * I'm sorry for this class name. I don't know what else to call it.
 */
public interface II18n {
    II18n IDENTITY = input -> input;

    String translate(String input);
}
