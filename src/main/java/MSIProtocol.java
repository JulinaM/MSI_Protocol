import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class MSIProtocol {

    private static final int PROCESSOR_COUNT = 4;
    private static final int MEMORY_SIZE = 4;

    private static int[] memory = {100, 200, 300, 400};
    private static String dataKey = "DATA_IN_MEMORY";

    private static HashMap<Integer, HashMap<String, CacheContent>> processorCache = new HashMap<>(PROCESSOR_COUNT);

    public static void main(String[] args) {
        System.out.println("************ MSI Protocol Simulator ************ \n");
        Scanner reader = new Scanner(System.in);
        initializeCache();
        int operationInput;
        do {
            System.out.println("\nPlease select an option to perform: \n" +
                    "\n\t1. Read " + "\n\t2. Write " + "\n\t3. Check Status of Cache Data" + "\n\t4. Quit");
            operationInput = reader.nextInt();
            System.out.println("Type 1 to 4 ");
            switch (operationInput) {
                case 1:
                    int processorReading;
                    int indexToRead;
                    do {
                        System.out.println("Select the processor (1 - 4): ");
                        while (!reader.hasNextInt()) {
                            System.out.println("Invalid Entry. Enter value Between 1 to 4.");
                            reader.next();
                        }
                        processorReading = reader.nextInt();
                    } while (processorReading < 1 || processorReading > 4);

                    do {
                        System.out.println("Select the memory index to read (0-3): ");
                        while (!reader.hasNextInt()) {
                            System.out.println("Invalid Entry. Enter value Between 0 to 3.");
                            reader.next();
                        }
                        indexToRead = reader.nextInt();
                    } while (indexToRead < 0 || indexToRead > 3);
                    readData(processorReading, indexToRead);
                    break;

                case 2:
                    int newValue;
                    int processorWriting;
                    int variableToWrite;

                    do {
                        System.out.println("Select the processor (1 - 4): ");
                        while (!reader.hasNextInt()) {
                            System.out.println("Invalid Entry. Enter value Between 1 to 4.");
                            reader.next();
                        }
                        processorWriting = reader.nextInt();
                    } while (processorWriting < 1 || processorWriting > 4);

                    do {
                        System.out.println("Select the memory index to write (0-3): ");
                        while (!reader.hasNextInt()) {
                            System.out.println("Invalid Entry. Enter value Between 0 to 3.");
                            reader.next();
                        }
                        variableToWrite = reader.nextInt();
                    } while (variableToWrite < 0 || variableToWrite > 3);

                    System.out.println("Enter the New Value for Data (Any Integer): ");
                    while (!reader.hasNextInt()) {
                        System.out.println("Invalid Entry. Enter an Integer Value.");
                        reader.next();
                    }
                    newValue = reader.nextInt();
                    writeData(newValue, processorWriting, variableToWrite);
                    break;

                case 3:
                    System.out.print("Data in Memory : "); displayBlock(memory);
                    for (int i = 1; i <= PROCESSOR_COUNT; i++) {
                        System.out.println();
                        System.out.printf("Processor %d: \n", i);
                        if (processorCache.get(i).containsKey(dataKey)) {
                            switch (processorCache.get(i).get(dataKey).cacheState) {
                                case INVALID:
                                    System.out.println("State: INVALID");
                                    break;
                                case SHARED:
                                    System.out.print("Data Value : "); displayBlock(processorCache.get(i).get(dataKey).value);
                                    System.out.println("State: SHARED");
                                    break;

                                case MODIFIED:
                                    System.out.print("Data Value : "); displayBlock(processorCache.get(i).get(dataKey).value);
                                    System.out.println("State: MODIFIED");
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            System.out.println("Data Not In Processor. " + "\nState: INVALID");
                        }
                    }
                    break;

                default:
                    break;
            }

        } while (operationInput < 4);
        reader.close();
    }

    private static void initializeCache() {
        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            processorCache.put(i, new HashMap<String, CacheContent>(MEMORY_SIZE));
        }
    }

    private static void readData(int processorReading, int indexToRead) {

        if (processorCache.get(processorReading).containsKey(dataKey)) {
            System.out.println("Data available in cache.");
            CacheContent currentCacheContent = processorCache.get(processorReading).get(dataKey);
            switch (currentCacheContent.cacheState) {
                case INVALID:
                    printReadMessage(CacheContent.CacheState.INVALID.toString(), CacheContent.CacheState.SHARED.toString(), "ReadMiss", "MEMORY");
                    sendReadMissRequest();
                    currentCacheContent.cacheState = CacheContent.CacheState.SHARED;
                    currentCacheContent.value = Arrays.copyOf(memory, MEMORY_SIZE);
                    System.out.printf("Read Data \t\t : %d ", currentCacheContent.value[indexToRead]);
                    break;
                case SHARED:
                    printReadMessage(CacheContent.CacheState.SHARED.toString(), CacheContent.CacheState.SHARED.toString(), "--", "LOCAL");
                    System.out.printf("Read Data \t\t : %d ", currentCacheContent.value[indexToRead]);
                    break;
                case MODIFIED:
                    printReadMessage(CacheContent.CacheState.MODIFIED.toString(), CacheContent.CacheState.MODIFIED.toString(), "--", "LOCAL");
                    System.out.printf("Read Data \t\t : %d ", currentCacheContent.value[indexToRead]);
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Data not available in cache.");
            printReadMessage(CacheContent.CacheState.INVALID.toString(), CacheContent.CacheState.SHARED.toString(), "ReadMiss", "MEMORY");
            sendReadMissRequest();
            processorCache.get(processorReading).put(dataKey, new CacheContent(Arrays.copyOf(memory, MEMORY_SIZE), CacheContent.CacheState.SHARED));
            System.out.printf("Read Data \t\t : %d ", memory[indexToRead]);
        }
    }

    private static void writeData(int newValue, int processorWriting, int variableToWrite) {
        if (processorCache.get(processorWriting).containsKey(dataKey)) {
            System.out.println("Data available in cache.");
            CacheContent currentCacheContent = processorCache.get(processorWriting).get(dataKey);
            switch (currentCacheContent.cacheState) {
                case INVALID:
                    printWriteMessage(CacheContent.CacheState.INVALID.toString(), CacheContent.CacheState.MODIFIED.toString(), "WriteMiss");
                    sendWriteMissRequest();
                    currentCacheContent.cacheState = CacheContent.CacheState.MODIFIED;
                    currentCacheContent.value = Arrays.copyOf(memory, MEMORY_SIZE);
                    currentCacheContent.value[variableToWrite] = newValue;
                    System.out.print("New Local Data \t : "); displayBlock(currentCacheContent.value);
                    break;
                case SHARED:
                    printWriteMessage(CacheContent.CacheState.SHARED.toString(), CacheContent.CacheState.MODIFIED.toString(), "Invalidation");
                    sendInvalidation();
                    currentCacheContent.cacheState = CacheContent.CacheState.MODIFIED;
                    currentCacheContent.value[variableToWrite] = newValue;
                    System.out.print("New Local Data \t : "); displayBlock(currentCacheContent.value);
                    break;
                case MODIFIED:
                    currentCacheContent.value[variableToWrite] = newValue;
                    printWriteMessage(CacheContent.CacheState.MODIFIED.toString(), CacheContent.CacheState.MODIFIED.toString(), "--");
                    System.out.print("New Local Data \t : "); displayBlock(currentCacheContent.value);
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Data not available in cache.");
            printWriteMessage(CacheContent.CacheState.INVALID.toString(), CacheContent.CacheState.MODIFIED.toString(), "WriteMiss");
            sendWriteMissRequest();
            int[] newData = Arrays.copyOf(memory, MEMORY_SIZE);
            newData[variableToWrite] = newValue;
            processorCache.get(processorWriting).put(dataKey, new CacheContent(newData, CacheContent.CacheState.MODIFIED));
            System.out.print("New Local Data \t : "); displayBlock(processorCache.get(processorWriting).get(dataKey).value);
        }
    }

    private static void sendInvalidation() {
        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            if(processorCache.get(i).containsKey(dataKey)) {
                processorCache.get(i).get(dataKey).cacheState = CacheContent.CacheState.INVALID;
            }
        }
    }

    private static void sendWriteMissRequest() {
        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            if (processorCache.get(i).containsKey(dataKey)){
                if (processorCache.get(i).get(dataKey).cacheState == CacheContent.CacheState.MODIFIED){
                    writeBackToMemory(processorCache.get(i).get(dataKey).value);
                }
                processorCache.get(i).get(dataKey).cacheState = CacheContent.CacheState.INVALID;
            }
        }
    }

    private static void sendReadMissRequest() {
        for (int i = 1; i <= PROCESSOR_COUNT; i++) {
            if (processorCache.get(i).containsKey(dataKey) && processorCache.get(i).get(dataKey).cacheState == CacheContent.CacheState.MODIFIED) {
                writeBackToMemory(processorCache.get(i).get(dataKey).value);
                processorCache.get(i).get(dataKey).cacheState = CacheContent.CacheState.SHARED;
            }
        }
    }

    private static void writeBackToMemory(int[] value) {
        memory = Arrays.copyOf(value, MEMORY_SIZE);
    }

    private static void displayBlock(int[] value){
        System.out.println(Arrays.toString(value));
    }

    private static void printReadMessage(String currStatus, String newStatus, String message, String location){

        System.out.println("Current state \t : "+ currStatus + "\n" +
                "State Changed \t : from "+ currStatus +" to "+ newStatus+ "\n" +
                "Message in Bus \t : "+ message + " \n" +
                "Action \t\t\t : Data read from the "+ location+ ".");
    }

    private static void printWriteMessage(String currStatus, String newStatus, String message){

        System.out.println("Current state \t : "+ currStatus + "\n" +
                "State Changed \t : from "+ currStatus +" to "+ newStatus+ "\n" +
                "Message in Bus \t : "+ message + " \n" +
                "Action \t\t\t : "+  "New value set to the cache.");
    }
}