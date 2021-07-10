import java.io.*;
import static java.lang.System.out;
import java.util.*;

//=====================================================================
class DictEntry2 {

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry2() {
        postingList = new HashSet<Integer>();
    }
}

//=====================================================================
class Index2 {
    
    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry2> index; // THe inverted index
    //--------------------------------------------

    Index2() {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry2>();
    }

    //---------------------------------------------
    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry2 dd = (DictEntry2) pair.getValue();
            HashSet<Integer> hset = dd.postingList;// (HashSet<Integer>) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
            Iterator<Integer> it2 = hset.iterator();
            while (it2.hasNext()) {
                System.out.print(it2.next() + ", ");
            }
            System.out.println("");
            //it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //-----------------------------------------------
    public void buildIndex(String[] files) {
        int i = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry2());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }
                //  printDictionary();
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
    }

    //--------------------------------------------------------------------------
    // query inverted index
    // takes a string of terms as an argument
    public String find(String phrase) {

        String result = "";
        String[] words = phrase.split("\\W+");
        try {
            HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
            for (String word : words) {
                res.retainAll(index.get(word).postingList);
            }
            result = "Found in: \n";
            for (int num : res) {
                result += "\t" + sources.get(num) + "\n";
            }
        } catch (Exception e) {
            System.out.println("Not found");
        }
        return result;
    }

    //----------------------------------------------------------------------------  
    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2) {
        HashSet<Integer> answer = new HashSet<Integer>();
        Iterator<Integer> p1 = pL1.iterator();
        Iterator<Integer> p2 = pL2.iterator();
        int docID1 = 0, docID2 = 0;
        if (p1.hasNext()) {
            docID1 = p1.next();
        }
        if (p2.hasNext()) {
            docID2 = p2.next();
        }
        while (p1.hasNext() && p2.hasNext()) {
            if (docID1 == docID2) {
                answer.add(docID1);
                docID1 = p1.next();
                docID2 = p2.next();
            } else if (docID1 < docID2) {
                if (p1.hasNext()) {
                    docID1 = p1.next();
                } else {
                    return answer;
                }
            } else {
                if (p2.hasNext()) {
                    docID2 = p2.next();
                } else {
                    return answer;
                }
            }
        }
        if (docID1 == docID2) {
            answer.add(docID1);
        }
        return answer;
    }
    // HashSet<Integer> answer = null;
    //        INTERSECT ( p1 , p2 )
    //          1 answer ←   {}
    //          2 while p1  != NIL and p2  != NIL
    //          3 do if docID ( p 1 ) = docID ( p2 )
    //          4   then A DD ( answer, docID ( p1 ))
    //          5       p1 ← next ( p1 )
    //          6       p2 ← next ( p2 )
    //          7   else if docID ( p1 ) < docID ( p2 )
    //          8        then p1 ← next ( p1 )
    //          9        else p2 ← next ( p2 )
    //          10 return answer
    //   return answer;

    //-----------------------------------------------------------------------   
    public String find_01(String phrase) { // 2 term phrase  2 postingsLists
        String result = "";
        String[] words = phrase.split("\\W+");
        try {
            // 1- get first posting list
            HashSet<Integer> pL1 = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
            // 2- get second posting list
            HashSet<Integer> pL2 = new HashSet<Integer>(index.get(words[1].toLowerCase()).postingList);
            // 3- apply the algorithm
            HashSet<Integer> answer = intersect(pL1, pL2);
            System.out.println("Found in: ");
            for (int num : answer) {
                //System.out.println("\t" + sources.get(num));
                result += "\t" + sources.get(num) + "\n";
            }
        } catch (Exception e) {
            System.out.println("Not found");
        }
        return result;
    }
//-----------------------------------------------------------------------         

    public String find_02(String phrase) { // 3 lists
        String result = "";
        try {
            // write you code here
            String[] words = phrase.split("\\W+");
            // 1- get first posting list
            HashSet<Integer> pL1 = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
            // 2- get second posting list
            HashSet<Integer> pL2 = new HashSet<Integer>(index.get(words[1].toLowerCase()).postingList);
            // 3- apply the algorithm
            HashSet<Integer> answer1 = intersect(pL1, pL2);
            HashSet<Integer> pL3 = new HashSet<Integer>(index.get(words[2].toLowerCase()).postingList);
            HashSet<Integer> answer2 = intersect(pL3, answer1);
            System.out.println("Found in: ");
            for (int num : answer2) {
                //System.out.println("\t" + sources.get(num));
                result += "\t" + sources.get(num) + "\n";
            }
        } catch (Exception e) {
            System.out.println("Not found");
        }
        return result;
    }
    //-----------------------------------------------------------------------    

    public String find_03(String phrase) { // any mumber of terms non-optimized search 
        String result = "";
        try {
            // write you code here
            String[] words = phrase.split("\\W+");
            int len = words.length;
            HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
            int i = 1;
            while (i < len) {
                res = intersect(res, index.get(words[i].toLowerCase()).postingList);
                i++;
            }
            for (int num : res) {
                result += "\t" + sources.get(num) + "\n";
            }
        } catch (Exception e) {
            System.out.println("Not found");
        }
        return result;
    }

    /* private static HashMap<String, Integer> arrange(HashMap<String, Integer> givenList) {
        // Create a list from elements of HashMap
        //   long startTime = System.nanoTime();
        List<Map.Entry<String, Integer>> unSortedList
                = new LinkedList<Map.Entry<String, Integer>>(givenList.entrySet());

        // Sort the list
        Collections.sort(unSortedList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> object1,
                    Map.Entry<String, Integer> object2) {
                return (object1.getValue()).compareTo(object2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> sortedList = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> iterator : unSortedList) {
            sortedList.put(iterator.getKey(), iterator.getValue());
        }
        return sortedList;
    }
    //-----------------------------------------------------------------------         

    public String find_4(String phrase) { // any mumber of terms optimized search 
        String result = "";
        // write you code here
        try {
            if (phrase == "") {
                return result;
            }

            String[] words = phrase.split("\\W+");
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (String word : words) {
                map.put(word, (index.get(word.toLowerCase()).postingList).size());
            }

            Map<String, Integer> sortedMap = arrange(map);
            HashSet<Integer> res = new HashSet<Integer>();
            for (String key : sortedMap.keySet()) {
                res = new HashSet<Integer>(index.get(key.toLowerCase()).postingList);
                break;
            }

            for (Object key : sortedMap.keySet()) {
                res = intersect(index.get(key.toString().toLowerCase()).postingList, res);
            }
            // System.out.println("Found in: ");
            for (int id : res) {
                result += "\t" + sources.get(id) + "\n";
            }

        } catch (Exception e) {
            System.out.println("Not found");
        }
      

        return result;

    }*/
    String[] rearrangeWords(String[] words, int[] frequency, int lenght) {
        boolean isSorted = false;
        int hold;
        String sHold;
        for (int i = 0; i < lenght - 1; i++) {
            frequency[i] = index.get(words[i].toLowerCase()).doc_freq;
        }
        while (isSorted) {
            isSorted = true;
            for (int i = 0; i < lenght - 1; i++) {
                if (frequency[i] > frequency[i + 1]) {
                    hold = frequency[i];
                    sHold = words[i];
                    frequency[i] = frequency[i + 1];
                    words[i] = words[i + 1];
                    frequency[i + 1] = hold;
                    words[i + 1] = sHold;
                    isSorted = false;
                }
            }
        }
        return words;
    }

    public String find_04(String phrase) {
        String result = "";
        try {
            // write you code here
            String[] words = phrase.split("\\W+");
            int len = words.length;
            words = rearrangeWords(words, new int[len], len);
            HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
            int i = 1;
            while (i < len) {
                res = intersect(res, index.get(words[i].toLowerCase()).postingList);
                i++;
            }
            for (int num : res) {
                result += "\t" + sources.get(num) + "\n";
            }
        } catch (Exception e) {
            System.out.println("Not found");
        }
        return result;
    }
    //-----------------------------------------------------------------------         
    //explain reselt
/*
    First case:- In case we put too much stop words or words that are in many files it will take much time because 
    intersection domin has many documents ids that i have path through them all,
    in this case optimized search  has the best time estimate and non-optimized search  is worst.
    
    Second case :- if the first word is the lowest frequency overall the phease so that it will be the first in the order list ,
    in this case optimized search  has the worst time estimate and non-optimized search  is better.
    
    Third case :- if every word has no intersection set due to each word appears once in diffrent files ,
    so that there is no solution. 

                              ( test cases ) 
                   case1 :- ehab on information system 
                  case 2:- privilege converting on
     */

    public void compare(String phrase) { // optimized search 
        //test  privilege converting on  optimized worst case
        String result = "";
        long iterations = 1000000;
        long startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find(phrase);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" find (*) elapsed = " + estimatedTime + " ms.");
        System.out.println(" result = " + result);

        startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find_03(phrase);
        }
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" (*) Find_03 non-optimized intersect  elapsed = " + estimatedTime + " ms.");
        System.out.println(" result = " + result);

        startTime = System.currentTimeMillis();
        for (long i = 1; i < iterations; i++) {
            result = find_04(phrase);
        }
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" (*) Find_04 optimized intersect elapsed = " + estimatedTime + " ms.");
        System.out.println(" result = " + result);
    }

}

//=====================================================================
public class InvertedIndex002 {

    public static void main(String args[]) throws IOException {
        Index2 index = new Index2();
        String phrase = "";
        index.buildIndex(new String[]{
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\100.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\101.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\102.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\103.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\104.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\105.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\106.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\107.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\108.txt",
            "F:\\Y3T2\\information retrieval\\work for assigment1 v1\\tmp\\109.txt"
        });

        while (true) {
            System.out.println("[1] find takes a string of terms as an argument");
            System.out.println("[2] find1 2 term phrase  2 postingsLists");
            System.out.println("[3] find2 3 term phrase  2 postingsLists");
            System.out.println("[4]find3 any mumber of terms non-optimized search ");
            System.out.println("[5]find4 any mumber of terms optimized search ");
            System.out.println("[6]compare (compar find vs find_03 vs find_04)");
            System.out.println("[7]Exit the program");
            Scanner input = new Scanner(System.in);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please Enter your choice ");
            int choice = input.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    out.println(index.find(phrase));
                    break;
                case 2:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    out.println(index.find_01(phrase));
                    break;
                case 3:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    out.println(index.find_02(phrase));
                    break;
                case 4:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    out.println(index.find_03(phrase));
                    break;
                case 5:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    out.println(index.find_04(phrase));
                    break;
                case 6:
                    System.out.println("Print search phrase: ");
                    phrase = in.readLine();
                    index.compare(phrase);
                    break;
                default:
                    out.println("GOOD BYE");
                    System.exit(0);    
            }
        }
    }
}
