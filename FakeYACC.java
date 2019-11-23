package fakeyacc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * convert a non-LL(1) grammar to a LL(1) grammar if it is possible
 */
public class FakeYACC {

    // NonTerminal  notice that modify the NonTerminal should change both the map and list
    private static Map<String, NonTerminal> NonTerminalMap = new HashMap<>();
    private static List<NonTerminal> NonTerminals;

    // Terminal
    private static Map<String,Terminal> TerminalMap = new HashMap<>();
    //ε and # will treat as Terminal

    private final String directory = "test2";//grammar

//    int order = 1;//the name of the left-part in the newly created production

    //warning:
    // the original grammar should satisfy the following two conditions,
    //
    // the right part of candidate can't consists of a single "ε"
    // no any NonTerminal possesses candidate like P → P
    private void readGrammar(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(directory +"/grammar"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(reader==null){ System.out.println("grammarReader is null"); return; }
        String line = null;
        try {
            NonTerminal nt = null;
            Candidate candidate = null;
            while ((line = reader.readLine()) != null) {
                if(line.equals(""))continue;
                String [] strings = line.split(" ");
                if(strings.length==1){//NonTerminal
                    if((nt = NonTerminalMap.get(strings[0]))==null){//arbitrary order
                        nt = new NonTerminal(strings[0]);
                        NonTerminalMap.put(strings[0],nt);
                    }
                }else {//Candidate of nt
                    candidate = new Candidate();
                    for(int i = 1;i<strings.length;i++){
                        Symbol symbol = TerminalMap.get(strings[i]);
                        if(symbol==null){
                            symbol = NonTerminalMap.get(strings[i]);
                        }
                        if(symbol==null){
                            symbol = new NonTerminal(strings[i]);
                            NonTerminalMap.put(strings[i], (NonTerminal) symbol);
                        }
                        candidate.symbols.add(symbol);
                    }
                    if(nt == null){ System.out.println("nt is null");return; }
                    nt.addCandidate(candidate);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        NonTerminals = new ArrayList<>(NonTerminalMap.values());
    }

    private void readTerminalSet(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(directory +"/terminalSet"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(reader==null){ System.out.println("grammarReader is null"); return;}
        String [] terminals = null;
        try {
            terminals = reader.readLine().split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(terminals==null){ System.out.println("terminals is null"); return;}
        for(String terminal : terminals){
            TerminalMap.put(terminal, new Terminal(terminal));
        }
        TerminalMap.put("ε",Terminal.nul);
        TerminalMap.put("$end",Terminal.sharp);
    }

    //exam FIRST collection and FOLLOW collection
    private void readFIRSTAndFOLLOW2Exam(boolean firstBool, boolean followBool){
        HashMap<String,Set<Terminal>> mapFIRST = new HashMap<>(512);
        HashMap<String,NonTerminal>  mapFOLLOW = new HashMap<>(512);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(directory +"/verification"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(reader==null){ System.out.println("grammarReader is null"); return;}
        String line = null;
        try {
            //init_declarator
            //nullable:No
            //firsts:IDENTIFIER,(,*
            //follows:,,;

            //              follow
            while((line = reader.readLine())!=null) {
                NonTerminal nt = new NonTerminal(line);
                Set<Terminal> first = new HashSet<Terminal>();

                //nullable:
                line = reader.readLine().substring(9);
                if(line.equals("No"))nt.deriveToNul = false;
                else if(line.equals("Yes")){ nt.deriveToNul = true;first.add(Terminal.nul); }
                else System.out.println("nullable error "+line);

                //first:
                line = reader.readLine().substring(7);
                if(line.contains(",,,")){
                    line = line.replace(",,,",",");
                    first.add(TerminalMap.get(","));
                }
                else if(line.contains(",,")){
                    line = line.replace(",,","");
                    first.add(TerminalMap.get(","));
                }
                if (line.length() == 1 && line.charAt(0) == ',') first.add(TerminalMap.get(","));
                else {
                    String[] ts = line.split(",");
                    for (String s : ts) {
                        first.add(TerminalMap.get(s));
                    }
                }

                //follow:
                line = reader.readLine().substring(8);
                if(line.contains(",,,")){
                    line = line.replace(",,,",",");
                    nt.FOLLOW.add(TerminalMap.get(","));
                }
                else if(line.contains(",,")){
                    line = line.replace(",,","");
                    nt.FOLLOW.add(TerminalMap.get(","));
                }
                if (line.length() == 1 && line.charAt(0) == ',') nt.FOLLOW.add(TerminalMap.get(","));
                else {
                    String[] ts = line.split(",");
                    for (String s : ts) {
                        nt.FOLLOW.add(TerminalMap.get(s));
                    }
                }

                mapFIRST.put(nt.name,first);
                mapFOLLOW.put(nt.name,nt);

                reader.readLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        if(firstBool){
            System.out.println("=======================FIRST VERIFICATION====================");
            for(NonTerminal nt : NonTerminals){
                Set<Terminal> first = mapFIRST.get(nt.name);
                if(first==null){
                    System.out.println(nt+"first null"); continue;}
                Set<Terminal> FIRST = new HashSet<Terminal>();
                for(Candidate candidate : nt.candidateList()){
                    FIRST.addAll(candidate.getFIRST());
                }
                if(FIRST.size()!=first.size()){
                    System.out.println();
                    System.out.println(nt);
                    ArrayList<Terminal> calculate = new ArrayList<>(FIRST);
                    calculate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                    System.out.println("calculate : "+calculate);
                    ArrayList<Terminal> generate = new ArrayList<>(first);
                    generate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                    System.out.println("generate  : "+generate);
                    continue;
                }

                for(Terminal t : FIRST){
                    if(!first.contains(t)){
                        System.out.println();
                        System.out.println(nt);
                        ArrayList<Terminal> calculate = new ArrayList<>(FIRST);
                        calculate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                        System.out.println("calculate : "+calculate);
                        ArrayList<Terminal> generate = new ArrayList<>(first);
                        generate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                        System.out.println("generate  : "+generate);
                        break;
                    }
                }
            }
        }
        if(followBool){
            System.out.println("=======================FOLLOW VERIFICATION====================");
            for(NonTerminal nt : NonTerminals){
                NonTerminal nnt = mapFOLLOW.get(nt.name);
                if(nnt==null){ System.out.println(nt);continue; }
                Set<Terminal> FOLLOW = nnt.FOLLOW;
                for(Terminal t : nt.FOLLOW){
                    if(!FOLLOW.contains(t)){
                        System.out.println();
                        System.out.println(nt);
                        ArrayList<Terminal> calculate = new ArrayList<>(nt.FOLLOW);
                        calculate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                        System.out.println("calculate : "+calculate);
                        ArrayList<Terminal> generate = new ArrayList<>(FOLLOW);
                        calculate.sort(Comparator.comparing(o -> ((Terminal) o).name));
                        System.out.println("generate  : "+generate);
                        break; }
                }
            }
        }
    }

    private static String printNonTerminalMap(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for(String key : NonTerminalMap.keySet()){
            sb.append(key);//key
            sb.append("=");
            NonTerminal nt = NonTerminalMap.get(key);
            sb.append(nt.toString());//value
            sb.append("->");
            for(Candidate candidate : nt.candidateList()){//value的展开
                sb.append(candidate.toString());
                sb.append("| ");
            }
            sb.delete(sb.length()-2,sb.length()-1);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String printNonTerminals() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for(NonTerminal nt : NonTerminals){
            sb.append('\t');
            sb.append(nt.toString());//value
            sb.append("->");
            for(Candidate candidate : nt.candidateList()){//value的展开
                sb.append(candidate.toString());
                sb.append("| ");
            }
            sb.delete(sb.length()-2,sb.length()-1);
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private static void printAsBisonBNF(){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for(NonTerminal nt : NonTerminals){
            sb.append(nt.name);
            sb.append("\n\t:");
            for(Candidate candidate : nt.candidateList()){
                for(Symbol symbol : candidate.symbols){
                    if(symbol.isNonTerminal()){
                        sb.append(" ");
                        sb.append(symbol.name);
                    }else {
                        sb.append(" ");
                        if(symbol.name.length()==1 && !symbol.equals(Terminal.nul))sb.append("'");
                        sb.append(symbol.name);
                        if(symbol.name.length()==1 && !symbol.equals(Terminal.nul))sb.append("'");
                    }
                }
                sb.append("\n");
                sb.append("\t|");
            }
            sb.setCharAt(sb.length()-1,';');
            sb.append("\n\n");
        }
        sb.append("}");
        System.out.println(sb.toString());
    }

    private static void printFIRST(){
        for(NonTerminal nt : NonTerminals){
            Set<Terminal> FIRST = new HashSet<Terminal>(64);
            for(Candidate candidate : nt.candidateList()){
                FIRST.addAll(candidate.getFIRST());
            }
            System.out.println(nt.name+" FIRST:"+FIRST);
        }
    }

    private static void printFOLLOW(){
        for(NonTerminal nonTerminal : NonTerminals){
            System.out.print(nonTerminal.name);
            System.out.print(" : ");
            System.out.println(nonTerminal.FOLLOW);
        }
    }

    //eliminate left recursion
    private void eliminateLeftRecursion(){
        boolean re = false;//recur flag
        for(int i = 0;i<NonTerminals.size();i++){//for every NonTerminal

            NonTerminal nonTerminal = NonTerminals.get(i);
            List<Candidate> candidates = nonTerminal.candidateList();

            //replace all candidate start with NonTerminal exists in Candidate before current NonTerminal
            for(int j = 0;j<candidates.size();j++){//for every candidate
                Symbol firstSymbol = candidates.get(j).firstSymbol();
                if(firstSymbol.isNonTerminal()){//if starts with NonTerminal
                    for(int k = 0;k<i;k++){
                        if(firstSymbol  == NonTerminals.get(k)){//start with NonTerminal that before current one
                            //replace Candidate Pjγ with Candidates ( δ1 | δ2 | … | δn )γ, Pj → ( δ1 | δ2 | … | δn )
                            candidates.addAll(NonTerminals.get(k).replaceCandidate(candidates.remove(j)));
                            j--;//Pi candidates.get(j) has removed
                            re = true;
                        }
                    }
                }
            }

            //eliminate direct left recursion
            Set<Integer> leftRecursionIndex = new TreeSet<>();
            for(int j = 0;j<candidates.size();j++){
                if(candidates.get(j).firstSymbol() == nonTerminal){//direct left recursion
                    leftRecursionIndex.add(j);
                }
            }
            if(!leftRecursionIndex.isEmpty()) {//eliminate
                re = true;
                NonTerminal newNonTerminal = null;
                String name = nonTerminal.name;
                do{
                    name += PostFix.ELRPostfix;//new NonTerminal postfix
                    newNonTerminal = NonTerminalMap.get(name);
                }while (newNonTerminal!=null);
                newNonTerminal = new NonTerminal(name);
                //P → Pa | Pb | c change to P → cP', P' → aP' | bP' | ε
                for (int j = 0; j < candidates.size(); j++) {
                    if (leftRecursionIndex.contains(j)) {//change candidate like Pa to aP', and add to newNonTerminal
                        newNonTerminal.addCandidate(candidates.get(j).changeCandidate(newNonTerminal));
                    } else {
                        candidates.get(j).symbols.add(newNonTerminal);//change candidate like c to cP'
                    }
                }
//                candidates.removeAll(newNonTerminal.candidateList());//may remove all the same Candidate
                for(Candidate candidate : newNonTerminal.candidateList()){
                    candidates.remove(candidate);
                }
                newNonTerminal.addCandidate(new Candidate(Terminal.nul));//new a candidate "ε"
                NonTerminalMap.putIfAbsent(newNonTerminal.name, newNonTerminal);
                NonTerminals.add(newNonTerminal);
            }
        }
        if(re)eliminateLeftRecursion();
    }

    //remove unreachable NonTerminals
    private void removeUnreachableNonTerminals() {
        Set<NonTerminal> abandonSet = new HashSet<>(NonTerminalMap.values());//grantee no repetition
        Queue<NonTerminal> queue = new ArrayDeque<NonTerminal>();//NonTerminals links with start
        NonTerminal nt = NonTerminalMap.get("translation_unit");//start unit
        queue.add(nt);
        if(!abandonSet.remove(nt)){ System.out.println("start NonTerminal is incorrect"); }
        while (queue.size()!=0){
            nt = queue.remove();
            for(Candidate candidate : nt.candidateList()){
                for(Symbol symbol : candidate.symbols){
                    if(symbol.isNonTerminal()){
                        if(abandonSet.remove((NonTerminal) symbol)){
                            queue.offer((NonTerminal) symbol);
                        }
                    }
                }
            }
        }
        for(NonTerminal nonTerminal : abandonSet){//drop all nonTerminals in abandonSet
            NonTerminalMap.remove(nonTerminal.name);
            NonTerminals.remove(nonTerminal);
        }
        System.out.println("=====================================");
        System.out.println("remove:");
        System.out.println(abandonSet);
        System.out.println("=====================================");
    }

    //extract left common factor
    private void extractLeftCommonFactor(){
        for(int i = 0;i<NonTerminals.size();i++){
            NonTerminal currentNonTerminal = NonTerminals.get(i);
            Map<List<Symbol>, Set<Integer>> map =  currentNonTerminal.getLeftCommonFactor();
            List<List<Symbol>> commonFactors = new ArrayList<>(map.keySet());
            commonFactors.sort((Comparator<List<Symbol>>) (o1, o2) -> map.get(o1).size()-map.get(o2).size());
            Set<Integer> extracted = new HashSet<>(currentNonTerminal.candidateList().size());
            //remove candidate after extract all common factors, otherwise indexes in currentNonTerminal candidates will change
            for(List<Symbol> symbols : commonFactors){
                Set<Integer> indexes = map.get(symbols);
                boolean extractable = true;
                HashMap<String, NonTerminal> newNonTerminalMap = new HashMap<>();
                for(int index : indexes){ if(extracted.contains(index)){ extractable = false;break; } }
                if(extractable){

                    NonTerminal newNonTerminal = currentNonTerminal.extractLeftCommonFactor(symbols,indexes,newNonTerminalMap);

                    extracted.addAll(indexes);
//                    System.out.println(newNonTerminal);
                    //can produce repeated production
                    if(NonTerminalMap.putIfAbsent(newNonTerminal.name, newNonTerminal)==null){
                        NonTerminals.add(newNonTerminal);//add newly NonTerminal to list
                    }
                }
            }//

            if(commonFactors.size()!=0)i--;
            List<Candidate> extractedCandidates = new ArrayList<>(extracted.size());
            for(int index : extracted){
                extractedCandidates.add(currentNonTerminal.candidateList().get(index));
            }
            currentNonTerminal.candidateList().removeAll(extractedCandidates);
        }
    }

    //judge weather a NonTerminal can derive to Nul or not, result stored in NonTerminal deriveToNul
    private void fillDomainDeriveToNul(){
        boolean changed = false;
        for(NonTerminal nt : NonTerminals){
            boolean deriveToNul = false;
            for(Candidate candidate : nt.candidateList()){
                if(candidate.deriveToNul()){ deriveToNul = true;break; }
            }
            if(nt.deriveToNul!=deriveToNul){
                nt.deriveToNul = deriveToNul;
                changed = true;break;
            }
        }
        if(changed)fillDomainDeriveToNul();
    }

    //build FIRST collection
    private void buildCollectionFIRST(){
        boolean changed = false;
        for(NonTerminal nonTerminal : NonTerminals){
            for(Candidate candidate : nonTerminal.candidateList()){//build FIRST collection for every candidate
                Set<Terminal> FIRST = candidate.getFIRST();
                int preSize = FIRST.size();
                for(Symbol symbol : candidate.symbols){
                    if(symbol.isTerminal()){
                        FIRST.add((Terminal) symbol);break;
                    }
                    else if(symbol.isNonTerminal()){//NonTerminal
                        NonTerminal currentSymbol = ((NonTerminal)symbol);
                        if(FIRST.contains(Terminal.nul)){
                            currentSymbol.addToFIRST(FIRST);
                        }else {
                            currentSymbol.addToFIRST(FIRST);
                            FIRST.remove(Terminal.nul);
                        }
                        if(!currentSymbol.deriveToNul)break;
                    }
                }
                if(candidate.deriveToNul())FIRST.add(Terminal.nul);
                if(FIRST.size()!=preSize){ changed = true;break; }
            }
        }
        if(changed)buildCollectionFIRST();
    }

    //build FOLLOW collection
    private void buildCollectionFOLLOW(){
        boolean changed = true;
        NonTerminal startNonTerminal = NonTerminalMap.get("translation_unit");
        startNonTerminal.FOLLOW.add(Terminal.sharp);
        List<Integer> sizes = new ArrayList<>(NonTerminals.size());
        for (NonTerminal nonTerminal : NonTerminals){
            sizes.add(nonTerminal.FOLLOW.size());
        }
        while(changed){
            changed = false;
            for (int i = 0; i < NonTerminals.size(); i++) {
                NonTerminals.get(i).buildFOLLOW();
                if(NonTerminals.get(i).FOLLOW.size()!=sizes.get(i)){
                    sizes.set(i,NonTerminals.get(i).FOLLOW.size());
                    changed = true;
                }
            }
        }
    }

    public static void main(String[] args) {
        FakeYACC driver = new FakeYACC();
        driver.readTerminalSet();
        driver.readGrammar();
        System.out.println(TerminalMap);
        System.out.println(printNonTerminals());
//        System.out.println("before eliminateLeftRecursion: number of NonTerminals:"+NonTerminals.size());
//        System.out.println("------------------------------------------------");
//        driver.eliminateLeftRecursion();//there is a doubt that P derives P', then weather P derives another P'
//        printAsBisonBNF();
//        System.out.println("after eliminateLeftRecursion: number of NonTerminals:"+NonTerminals.size());
//        System.out.println("the identity between NonTerminalMap & NonTerminals:"+driver.examNonTerminalMapAndNonTerminals());
//        System.out.println(printNonTerminals());
        System.out.println("clean unreachable NonTerminals after ELR");
        //test weather the algorithm that judges certain productions is unreachable is correct or not
/*        NonTerminal []island = new NonTerminal[5];
//        island[0] = new NonTerminal("0");
//        for(int i = 1;i<island.length;i++){
//            island[i] = new NonTerminal(String.valueOf(i));
//            island[i].addCandidate(new Candidate(island[i-1]));
//        }
//        island[0].addCandidate(new Candidate(island[island.length-1]));
//        for(NonTerminal nt : island){ NonTerminalMap.put(nt.name,nt);NonTerminals.add(nt); }
*/      //test
        System.out.println("before remove unreachable: number of NonTerminals:"+NonTerminals.size());
        driver.removeUnreachableNonTerminals();
        System.out.println("------------------------------------------------");
        System.out.println("after remove unreachable: number of NonTerminals:"+NonTerminals.size());
        System.out.println("the identity between NonTerminalMap & NonTerminals:"+driver.examNonTerminalMapAndNonTerminals());
        System.out.println("------------------------------------------------");
        System.out.println("before extract left common factors, number of NonTerminals:"+NonTerminals.size());
//        driver.extractLeftCommonFactor();
        printAsBisonBNF();
//        System.out.println(printNonTerminals());
        System.out.println("after extract left common factors, number of NonTerminals:"+NonTerminals.size());
        System.out.println("the identity between NonTerminalMap & NonTerminals:"+driver.examNonTerminalMapAndNonTerminals());
        System.out.println("------------------------------------------------");
        driver.fillDomainDeriveToNul();
        System.out.println("-------------------begin--build FIRST Collection------------------------");
        driver.buildCollectionFIRST();
        System.out.println("FIRST collection has no intersection:"+driver.examIntersectionOfFISRTCollection());
//        System.out.println("---------------------------FIRST  Collection-------------------------------");
//        printFIRST();
        System.out.println("exam the correctness of NonTerminals :" + driver.examDeriveToNul());
//        printAsBisonBNF();
        driver.buildCollectionFOLLOW();
//        System.out.println("---------------------------FOLLOW  Collection-------------------------------");
//        printFOLLOW();
        driver.readFIRSTAndFOLLOW2Exam(true,true);
        System.out.println();
    }

    //exam weather deriveToNul is correct or not
    private boolean examDeriveToNul(){
        for(NonTerminal nonTerminal : NonTerminals){
            boolean contains = false;
            for(Candidate candidate : nonTerminal.candidateList()){
                if(candidate.containsFIRST(Terminal.nul)){
                    contains = true;break;
                }
            }
            if(contains!=nonTerminal.deriveToNul){
                System.out.println("examDeriveToNul false:" + nonTerminal);
                return false;
            }
        }
        return true;
    }

    //exam the identity between NonTerminalMap & NonTerminals
    private boolean examNonTerminalMapAndNonTerminals(){
        if(NonTerminals.size()==NonTerminalMap.values().size()){
            for(NonTerminal nt : NonTerminals){
                if(!NonTerminalMap.values().contains(nt))return false;
            }
            return true;
        }
        return false;
    }

    //exam there is no intersection of any two candidates' FIRST collection of identical NonTerminal
    private boolean examIntersectionOfFISRTCollection(){
        int cnt = 0;
        boolean re = true;
        for(NonTerminal nonTerminal : NonTerminals){
            List<Candidate> candidates = nonTerminal.candidateList();
            for(int i = 0;i<candidates.size()-1;i++){
                for(int j = i+1;j<candidates.size();j++){
                    Candidate anotherCandidate = candidates.get(j);
                    for(Terminal terminal : candidates.get(i).getFIRST()){
                        if(anotherCandidate.containsFIRST(terminal)){
                            re = false;
                            cnt++;
                            System.out.println("conflict:" + nonTerminal.name + "中 :" + candidates.get(i) + "与 " + anotherCandidate);
                        }
                    }
                }
            }
        }
        System.out.println("conflict: "+cnt);
        return re;
    }

}