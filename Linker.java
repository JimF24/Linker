//JiaYi Feng
//jf3354
//0916-2018
//implementation of two-pass linker
import java.io.*;
import java.util.*;
import java.util.regex.*;
public class Linker {
	static int memorySize = 300;
	static int moduleNum = 0;
	//stores symbol and its value;
	static HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();
	//stores the location of every modules
	static ArrayList<Integer> StartLoc = new ArrayList<Integer>();
	static int address = 0;
	static  ArrayList<ArrayList<String>> GlobalList = new ArrayList<ArrayList<String>>();
	static ArrayList<Integer>	instructionList = new ArrayList<Integer>();
	//Scanner initialization
	
	public static  Scanner newScanner(String inputFile){
		try{
			Scanner input = new Scanner(new BufferedReader(new FileReader(inputFile)));
			return input;
		}
		catch(Exception ex){
			System.out.println("Error reading: " + inputFile);
			System.exit(0);
		}
		return null;
	}
	
	//First Pass, goal: record location of the modules, symbols and their values.
	//param: input(Scanned file)
	public static void FirstPass(Scanner input){
		int lineCount = 0;
		int modules = -1;
		int inc = -1;
		int absAddress = 0;
		ArrayList<String> symbolList = new ArrayList<String>();
		ArrayList<String> useList = new ArrayList<String>();
		ArrayList<String> infoList = new ArrayList<String>();
		String ProcessingLine = "";
		String[] info = null;
		//delete all of the white spaces.
		String Delimiter = "\\s+";
		char currChar;
		
		while(input.hasNext()){
			String currLine = input.next();
			info = currLine.split(Delimiter);
			for(int i = 0; i<info.length; i++){
				//if not null
				if(!info[i].equals("")){
					//add to the list
					infoList.add(info[i]);
				}
			}
		}
		for (int i = 0; i < infoList.size();){
			GlobalList.add(new ArrayList<String>());
			int sizeOfLine = 0;
			ProcessingLine = (String) infoList.get(i);
			if(i == 0){
				GlobalList.get(lineCount).add((String)infoList.get(i));
				i++;
			}
			int IntLine = Integer.parseInt(ProcessingLine);
			
			//first line of each module is definiton, then use, then instruction, so we can mod 3 to separate every line.
			if(lineCount % 3 ==1){
				sizeOfLine = 2*IntLine+1;
				//if there is no definition, then the size of the line should be 0
				if(IntLine == 0 ){
					GlobalList.get(lineCount).add("0");
					i++;
				}
				else{
					
					while(sizeOfLine>0){
						GlobalList.get(lineCount).add((String)infoList.get(i));
						sizeOfLine --;
						i++;
					}
				}
			}
			else if(lineCount %3 ==2){
				sizeOfLine = IntLine;
				if(sizeOfLine == 0 ){
					GlobalList.get(lineCount).add("0");
					i++;
				}
				else{
					while(sizeOfLine>0){
						GlobalList.get(lineCount).add((String)infoList.get(i));
						//use List ends when there is a -1.
						if(infoList.get(i).equals("-1")){
							sizeOfLine --;
						}
						i++;
					}
				}
			}
			else if(lineCount %3 ==0 && lineCount>0){
				sizeOfLine = IntLine +1;
				if(sizeOfLine == 0){
					GlobalList.get(lineCount).add("0");
					i++;
				}
				
					while(sizeOfLine>0){
						GlobalList.get(lineCount).add((String)infoList.get(i));
						sizeOfLine--;
						i++;
					}
				
			}
			lineCount++;
			
			//end of dividing def list, use list and instruction list
			
		}
		
		for(int i = 0; i<lineCount; i++){
			for(String s: GlobalList.get(i)){
				System.out.print(s + " ");
			}
			System.out.println("");
		}
		
		//process processed input file
		//that means pass the input file for the first time
		for(int i = 0; i < lineCount; i++){
			if(i == 0){
				
			}
			if(i ==1){
				StartLoc.add(address);
			}
			// def list
			if(i % 3 == 1){
				modules +=1;
				inc = 0;
				for(String currString: GlobalList.get(i)){
					currChar = GlobalList.get(i).get(inc).charAt(0);
					inc++;
					if(Character.isLetter(currChar)){
						if(!symbolTable.containsKey(currString)){
							String symNum = GlobalList.get(i).get(inc);
							int sizeOfMod = (GlobalList.get(i+2).size() - 1);
							// this int is the relative address
							int relAddress = Integer.parseInt(symNum);
							//check memory size
							if(relAddress > sizeOfMod-1){
								System.out.println("The size of address is bigger than the module size");
								absAddress = sizeOfMod-1 + (Integer)StartLoc.get(modules);
								symbolTable.put(currString, absAddress);
								symbolList.add(currString);
							}
							else{
								absAddress = relAddress+(Integer)StartLoc.get(modules);
								symbolTable.put(currString, absAddress);
								symbolList.add(currString);
							}
						}
						//else it is multiply defined
						else if(symbolTable.containsKey(currString)){
							System.out.println("Error: "+"The symbol"+currString+" is multiply defined. "+"Last value is used");
							
						}
					}
				}
			}
			//use list
			//Since it's the first pass, we don't have to do anything with the list but store it.
			else if(i % 3 ==2){
				for(String currString: GlobalList.get(i)){
					currChar = currString.charAt(0);
					if(Character.isLetter(currChar)){
						if(!useList.contains(currString)){
							useList.add(currString);
						}
					}
				}
			}
			// instruction list
			//again since it's the first pass, we don't have to do anything with the list but to record the end location of the module.
			else if (i%3==0 &&i>0){
				String currString = GlobalList.get(i).get(0);
				char convChar = currString.charAt(0);
				if (convChar<= '9' && convChar >= '0' || convChar == 45){
					int convInt = Integer.parseInt(currString);
					address = address+convInt;
					StartLoc.add(address);
				}
				inc = 0;
				for(String currStr: GlobalList.get(i)){
					if((inc < GlobalList.get(i).size() )&& (inc!=0) ){
						instructionList.add(Integer.parseInt(currStr));
						
					}inc++;
				}
			}
		}
		//check error type: symbol defined but not used
		for(int i = 0; i < symbolTable.size(); i++){
			if(!useList.contains(symbolList.get(i))){
				System.out.println("Error: "+symbolList.get(i) + "is defined but not used");
			}
		}
		
		
	}
	public static void secondPass(){
		ArrayList useList = new ArrayList<List>();
		ArrayList<Integer> finalList = new ArrayList<Integer>();
		int modNum = -1;
		int lineCount = 0;
		int inc = 0;
		int numValues = 0;
		int address = 0;
		int convInt = 0;
		int printAddr = 0;
		HashMap<Integer, String> usedMap = new HashMap<Integer, String>();
		for( lineCount = 0; lineCount < GlobalList.size(); lineCount++){
			if(lineCount == 0){continue;}
			// def list; add modNum
			if(lineCount%3 == 1){
				modNum ++;
			}
			
			//use list
			else if(lineCount % 3 == 2){
				String workingSymbol = null;
				int index = 0;
				for(String currString: GlobalList.get(lineCount)){
					if(index == 0){
						index++;
						continue;
					}
					char currChar = currString.charAt(0);
					if(Character.isLetter(currChar) ){
						if(symbolTable.containsKey(currString)){
							workingSymbol = currString;
						}
						else{
							System.out.println("Error: "+ currString + "was used but not defined. So value 111 is assigned");
							symbolTable.put(currString, 111);
						}
						workingSymbol = currString;
					}
					
					else if((Integer.parseInt(currString)!=-1)){
						int absadd = StartLoc.get(modNum)+(Integer.parseInt(currString));
						if(usedMap.containsKey(absadd)){
							System.out.println("Error: Multiple symbols are used. all are ignored but the last one");
							usedMap.replace(absadd, workingSymbol);
						}
						else{usedMap.put(absadd, workingSymbol);}
					}
					
					
					
				}
			}
		}
			//process instruction list separately
				
			
				for(int i = 0; i < instructionList.size(); i++){
//					System.out.println(i);
//					System.out.println(StartLoc.size());
					int instCode = (Integer)(instructionList.get(i));
					int type = instCode % 10;
					int finalNum = 0;
					int symValue = 0;
					int increment = 1;
					//type 1 immediate
					if(type ==1){
						finalNum = instCode/10;
						finalList.add(finalNum);
					}
					//type 2 Absolute
					if(type ==2){
						finalNum = instCode/10;
						//Out of memory error
						if(finalNum % 1000>=memorySize){
							System.out.println("Error: Absolute address of "+finalNum+"is out of machine size, largest machine size is used");
							finalNum = finalNum - (finalNum%1000);
							finalNum = finalNum+299;
							
						}
						
						finalList.add(finalNum);
						
					}
					
					//type 3 Relative
					if(type == 3){
						finalNum = (instCode)/10;
						//compute modNum
						int moduleN = 0;
						for(moduleN = 0; moduleN<StartLoc.size();moduleN++){
							if(StartLoc.get(moduleN)>i){
								break;
							}
						}
						moduleN--;

						
						
						finalNum = finalNum+StartLoc.get(moduleN);
						finalList.add(finalNum);
						
					}
					if(type == 4){
						finalNum = (instCode)/10;
						finalNum = finalNum - finalNum%1000;
						String symbolUsed = usedMap.get(i);
						int symbolValue = symbolTable.get(symbolUsed);
						finalNum = finalNum + symbolValue;
						if(finalNum % 1000>=memorySize){
							System.out.println("Error: Absolute address of "+finalNum+"is out of machine size, largest machine size is used");
							finalNum = finalNum - (finalNum%1000);
							finalNum = finalNum+299;
							
						}
						
						finalList.add(finalNum);
					}
				}
				
		
			
		
		for (int i = 0; i < finalList.size(); i++){
			System.out.printf(String.format("%3d", printAddr));
			System.out.print(": " + finalList.get(i));
			System.out.println();
			printAddr++;
		}
	}
	public static void printMap(Map<?,?>map){
		System.out.println();
		System.out.println("Symbol Table:");
		for(Map.Entry<?, ?>entry: map.entrySet()){
			System.out.println(" "+entry.getKey()+ " = "+entry.getValue());
			
		}
	}
	public static void main(String[] args){
		System.out.println("enter file name (i.e. input-1)");
		Scanner in = new Scanner (System.in);
		
			String filename = in.nextLine();
			Scanner input = newScanner (filename);
			FirstPass(input);
			printMap(symbolTable);
			secondPass();
		
		
		
		
		
	}
}
