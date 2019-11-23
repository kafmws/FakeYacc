package fakeyacc;

/*
 * Terminal      unique
 *
 * NonTerminal   unique
 *
 * Candidate     not unique
 *
 * */

public abstract class Symbol {

    //Terminal or NonTerminal
    String name;

    Symbol(String name) { this.name = name; }

    //return true if grammatical symbols is terminal
    boolean isTerminal() { return this instanceof Terminal&& !name.equals("ε"); }

    //return true if grammatical symbols is NonTerminal
    boolean isNonTerminal() { return this instanceof NonTerminal; }

    @Override
    public String toString() { return name; }
}

