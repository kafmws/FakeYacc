package FakeYacc;

import java.util.*;

/*
 * Terminal      unique
 *
 * NonTerminal   unique
 *
 * Candidate     not unique
 *
 * */

public class NonTerminal extends Symbol {

    private List<Candidate> candidates = new LinkedList<>();
    Set<Terminal> FOLLOW = new HashSet<>();
    boolean deriveToNul;

    NonTerminal(String name) { super(name); }

    private NonTerminal(String name, List<Candidate> candidates) {
        super(name);
        this.candidates = candidates;
    }

    boolean addCandidate(Candidate content) { return this.candidates.add(content); }

    boolean removeCandidate(Candidate content) { return this.candidates.remove(content); }

    boolean containsCandidate(Candidate content) { return this.candidates.contains(content); }

    Candidate getContainsFIRSTCandidate(Terminal s) {
        for (Candidate candidate : candidates) {
            if (candidate.containsFIRST(s)) return candidate;
        }
        return null;
    }

    //add FIRST to given FIRST
    void addToFIRST(Set<Terminal> FIRST){
        for(Candidate candidate : candidates){
            FIRST.addAll(candidate.getFIRST());
        }
    }

    boolean addToFOLLOW(Terminal terminal) { return FOLLOW.add(terminal); }

    boolean containsFOLLOW(Terminal terminal) { return FOLLOW.contains(terminal); }

    List<Candidate> candidateList(){ return candidates; }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)return true;
        if(obj instanceof NonTerminal){
            return ((NonTerminal)obj).name.equals(name);
        }
        return false;
    }

    //from Candidate Pi → Pjγ to Candidates Pi → ( δ1 | δ2 | … | δn )γ, Pj → ( δ1 | δ2 | … | δn )
    List<Candidate> replaceCandidate(Candidate Pj){
        List<Candidate> re = new ArrayList<>();
        for(Candidate candidate : candidateList()){
            Candidate newCandidate = new Candidate(candidate);
            for(int i = 1;i<Pj.symbols.size();i++){
                newCandidate.symbols.add(Pj.symbols.get(i));
            }
            re.add(newCandidate);
        }
        return re;
    }

    //return map that maps left common factors and its indexes
    Map<List<Symbol>, Set<Integer>> getLeftCommonFactor(){
        Map<List<Symbol>, Set<Integer>> map = new HashMap<>();//<commonFactors, indexes>
        int size = candidateList().size();
        for(int i = 0;i<size-1;i++){
            for(int j = i+1;j<size;j++){
                List<Symbol> commonFactors = candidates.get(i).getLeftCommonFactor(candidates.get(j));
                Set<Integer> indexes = map.get(commonFactors);
                if(indexes==null){if(commonFactors!=null)map.put(commonFactors, new HashSet<>(List.of(i,j)));}
                else { indexes.add(i);indexes.add(j); }
            }
        }
        return map;
    }

    //return true if the nonTerminal owns equivalent candidates with current one
    boolean examEquivalentNonTerminal(List<Candidate> candidateList){
//        List<Candidate> candidateList = nonTerminal.candidateList();
        if(candidateList.size()!=candidates.size())return false;
        List<Candidate> temCandidates = new ArrayList<>(candidates);
        List<Candidate> temCandidateList = new ArrayList<>(candidateList);
        temCandidates.sort(Candidate::compareTo);
        temCandidateList.sort(Candidate::compareTo);
        for(int i = 0;i<temCandidates.size();i++){
            if(!temCandidates.get(i).equals(temCandidateList.get(i)))return false;
        }
        return true;
    }

    //during A → δβ1 | δβ2 | … | δβn | δ | γ1 | γ2 | … | γm to A → δA' | γ1 | γ2 | … | γm, A' → β1 | β2 | … | βn | ε
    //(symbols, indexes, newNonTerminalMap) (leftCommonFactors, indexes of LCF in current NonTerminal, newly NonTerminal in current loop)
    //return new NonTerminal or the equivalent NonTerminal
    NonTerminal extractLeftCommonFactor(List<Symbol> symbols, Set<Integer> indexes, HashMap<String, NonTerminal> newNonTerminalMap){
        List<Candidate> newCandidates = new LinkedList<>();
        for(int index : indexes){
            newCandidates.add(this.candidates.get(index).removeLeftCommonFactor(symbols));
        }
        //decide weather to new a newly NonTerminal or not
        NonTerminal nt = null;
        for(NonTerminal nonTerminal : newNonTerminalMap.values()){
            if(nonTerminal.examEquivalentNonTerminal(newCandidates)){
                nt = nonTerminal;
                break;
            }
        }
        if(nt == null){//new a necessary newly NonTerminal
            String name = this.name;//may repeat
            do{
                name += PostFix.ELCPostfix ;//distinct from newly NonTerminals generate from elr
                nt = newNonTerminalMap.get(name);
            } while(nt!=null);
//            if(name.equals("struct_or_union_specifier2"))
//                System.out.println("debug");
            nt = new NonTerminal(name, newCandidates);
            newNonTerminalMap.put(nt.name, nt);
        }
        symbols.add(nt);//change left common factors δ to δA'
        this.candidates.add(new Candidate(symbols));//add δA' to A
        return nt;
    }

    //return true if its FOLLOW collection changed
    boolean buildFOLLOW(){
        boolean changed = false;
        for(Candidate candidate : candidateList()){
            if(candidate.buildFOLLOW(this)){
                changed = true;
            }
        }
        return changed;
    }

    Set<Terminal> getFIRST(){
        Set<Terminal> FIRST = new HashSet<>();
        for(Candidate candidate : candidates){
            FIRST.addAll(candidate.getFIRST());
        }
        return FIRST;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(name);
//        sb.append("→");
//        for(Candidate candidate : candidates){
//            sb.append(candidate.);
//            sb.append(" | ");
//        }
//        sb.delete(sb.length()-3,sb.length());
//        return sb.toString();
//    }
}

