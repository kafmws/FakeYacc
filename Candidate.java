package fakeyacc;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * Terminal      unique
 *
 * NonTerminal   unique
 *
 * Candidate     not unique
 *
 * */

/*
 * Candidate does not grantee unique
 * */
public class Candidate {

    List<Symbol> symbols;

    private Set<Terminal> FIRST;

    boolean deriveToNul;

    Set<Terminal> getFIRST(){ return FIRST; }

    boolean addFIRST(Terminal first) { return this.FIRST.add(first); }

    boolean containsFIRST(Terminal first) { return this.FIRST.contains(first); }

    Symbol firstSymbol(){ return symbols.get(0); }

    Candidate(Candidate candidate){
        this.symbols = new LinkedList<>(candidate.symbols);
        this.FIRST = new HashSet<>();
    }

    Candidate(List<Symbol> symbols){
        this.symbols = symbols;
        this.FIRST = new HashSet<>();
    }

    Candidate(Symbol firstSymbol){
        this.symbols = new LinkedList<>();
        this.symbols.add(firstSymbol);
        this.FIRST = new HashSet<>();
    }

    Candidate() {
        this.symbols = new LinkedList<>();
        this.FIRST = new HashSet<>();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)return true;
        if(obj instanceof Candidate){
            List<Symbol> symbolList = ((Candidate)obj).symbols;
            if(symbolList.size()!=symbols.size())return false;
            for(int i = 0;i<symbols.size();i++){
                if(!symbolList.get(i).equals(symbols.get(i)))return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Symbol symbol : symbols){
            sb.append(symbol.toString());
            sb.append(" ");
        }
        return sb.toString();
    }

    //P → Pa | Pb | c change to P → cP', P' → aP' | bP' | ε
    //changeCandidate change Pa to aP' and return the candidate after modify
    Candidate changeCandidate(NonTerminal nonTerminal){
        this.symbols.remove(0);
        this.symbols.add(nonTerminal);
        return this;
    }

    public boolean startWith(List<Symbol> symbols){
        if(symbols.size()>this.symbols.size())return false;
        for(int i = 0;i<symbols.size();i++){
            if(symbols.get(i)!=this.symbols.get(i))return false;
        }
        return true;
    }

    List<Symbol> getLeftCommonFactor(Candidate candidate){
        int index = 0;
        int size = Math.min(symbols.size(),candidate.symbols.size());
        while(index<size&&symbols.get(index)==candidate.symbols.get(index))index++;
        if(index==0)return null;
        return new LinkedList<Symbol>(symbols.subList(0,index));//[0,index)
    }

    Candidate removeLeftCommonFactor(List<Symbol> commonFactors){
        int i = 0;
        while (i<commonFactors.size()){ symbols.remove(0);i++; }
        if(symbols.isEmpty())symbols.add(Terminal.nul);
        return this;
    }

    //return true if a Candidate can infer to Nul
    boolean deriveToNul(){
        boolean re = true;
        if(firstSymbol()!=Terminal.nul){//assume there is no production like P → ε....
            for(Symbol symbol : symbols){
                if(symbol.isNonTerminal()){
                    if(!((NonTerminal)symbol).deriveToNul){ re = false; break; }
                }else { re = false; break; }
            }
        }
        deriveToNul = re;
        return re;
    }

    //buildFOLLOW
    //return true if any FOLLOW collection changed
    boolean buildFOLLOW(NonTerminal nt){
        boolean changed = false;
        int preSize;
        Symbol preSymbol = symbols.get(0);
        for(int i = 1;i<symbols.size();i++){
            if(preSymbol.isNonTerminal()){//A → αBβ  add FIRST(β)-{ε} to FOLLOW(B)
                Set<Terminal> preFOLLOW = ((NonTerminal)preSymbol).FOLLOW;
                preSize = preFOLLOW.size();
                boolean containsNul = preFOLLOW.contains(Terminal.nul);
                out:
                for(int j = i;j<symbols.size();j++){//add FIRST(β)-{ε} to FOLLOW(B)
                    if(symbols.get(j).isTerminal()){
                        if(symbols.get(j)!=Terminal.nul){
                            preFOLLOW.add((Terminal)symbols.get(j));
                            break;
                        }
                    }else{
                        for(Candidate candidate : ((NonTerminal)symbols.get(j)).candidateList()){
                            preFOLLOW.addAll(candidate.FIRST);
                            if(!((NonTerminal)symbols.get(j)).deriveToNul)break out;
                        }
                    }
                }
                if(!containsNul){//FIRST(symbol) - nul
                    preFOLLOW.remove(Terminal.nul);
                }
                if(preSize!=preFOLLOW.size()){
                    changed = true;
                }
            }
            preSymbol = symbols.get(i);
        }
        //A → αB || A → αBβ && β ⇒* ε, add FOLLOW(A) to FOLLOW(B)
        //here  preSymbol == lastSymbol
        if(preSymbol.isNonTerminal()){//handel the last one individually
            ((NonTerminal)preSymbol).FOLLOW.addAll(nt.FOLLOW);
            if(((NonTerminal)preSymbol).deriveToNul)
                for(int i = symbols.size()-2;i>=0;i--){
                    if(symbols.get(i).isNonTerminal()){
                        preSize = ((NonTerminal)symbols.get(i)).FOLLOW.size();
                        ((NonTerminal)symbols.get(i)).FOLLOW.addAll(nt.FOLLOW);
                        if(preSize!=((NonTerminal)symbols.get(i)).FOLLOW.size())changed = true;
                        if(!((NonTerminal)symbols.get(i)).deriveToNul)break;
                    }else break;
                }
        }
        return changed;
    }
}

