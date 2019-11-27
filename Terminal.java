package FakeYacc;

/*
 * Terminal      unique
 *
 * NonTerminal   unique
 *
 * Candidate     not unique
 *
 * */

public class Terminal extends Symbol {

    static Terminal nul = new Terminal("ε");
    static Terminal sharp = new Terminal("$end");

    Terminal(String name) { super(name); }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)return true;
        if(obj instanceof Terminal){
            return ((Terminal)obj).name.equals(name);
        }
        return false;
    }
}

