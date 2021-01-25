import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;

public class Store extends TimerTask {
    private Map<Integer, String> info;
    private List<Integer> deleteKey;
    private String filename;
    private Timer timer = new Timer();

    public Store(String filename) {
        this.info = new HashMap<>();
        this.deleteKey = new ArrayList<>();
        this.filename = filename;
        this.timer.schedule(this, 10000, 10000);

        this.init();
    }

    private void init() {
        File f = new File(this.filename);
        if(!f.exists()) {
            try {
                f.createNewFile();
            }catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void add(int key, String value) {

        // Store it in the memory (HashMap)
        // Step 1: check whether the key exsit, if exist, can not add again.
        if(this.query(key) == null) {
            this.info.put(key, value);
        }else {
            System.out.println("The key " + key + " already exists.");
        }
    }

    public void delete(int key) {
        this.info.remove(key);
        boolean whetherInDeleteKeys = false;
        for(int i = 0; i < this.deleteKey.size(); i++) {
            if(this.deleteKey.get(i) == key) {
                whetherInDeleteKeys = true;
                break;
            }
        }
        if(whetherInDeleteKeys == false) {
            this.deleteKey.add(key);
        }

    }

    public String query(int key) {
        // Check the hashmap directly, if no such a key, then go to the disk file to check.
        // Step 1: check the HashMap
        boolean whetherInMemory = this.info.containsKey(key);
        String result = null;

        // Step 2: check the disk
        if(whetherInMemory == false) {
            // If the delete key list has the key, the key must disappear
            boolean whetherInDeleteKeys = false;
            for(int i = 0; i < this.deleteKey.size(); i++) {
                if(this.deleteKey.get(i) == key) {
                    whetherInDeleteKeys = true;
                    break;
                }
            }
            // Read the file line by line
            if(whetherInDeleteKeys == true) {
                result= null;
            }else {
                try {
                    Scanner sc = new Scanner(new File(this.filename));
                    while(sc.hasNextLine()) {
                        String line = sc.nextLine();
                        if(Integer.parseInt(line.split(" ")[0]) == key) {
                            result = line.split(" ")[1];
                            break;
                        }
                    }
                }catch(FileNotFoundException e) {
                    // Do nothing
                }
            }

        }else {
            //Do nothing
            result = this.info.get(key);
        }
        return result;
    }

    public void update(int key, String value) {
        if(this.query(key) != null) {
            this.info.put(key, value);
        }else {
            System.out.println("The key " + key + " can not been found out.");
        }
    }

    public void save() {
        System.out.println("Save");

        // Step 1: Process Delete List
        for(int i = 0; i < this.deleteKey.size(); i++) {
            this.deleteLine(this.deleteKey.get(i));
        }

        // Step 2: Process HashMap, devided by update and add
        for(Entry<Integer, String> entry : this.info.entrySet()) {
            if(this.whetherInDisk(entry.getKey()) == true) {
                this.replaceValue(entry.getKey(), entry.getValue());
            }else {
                this.addNewLine(entry.getKey(), entry.getValue());
            }
        }

        // Step 3: Clear HashMap and Delete List
        this.deleteKey = new ArrayList<>();
        this.info = new HashMap<>();
    }

    private boolean whetherInDisk(int key) {
        boolean result = false;
        try {
            Scanner sc = new Scanner(new File(this.filename));
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                if(Integer.parseInt(line.split(" ")[0]) == key) {
                    result = true;
                    break;
                }
            }
        }catch(FileNotFoundException e) {
            // Do nothing
        }
        return result;
    }

    private void addNewLine(int key, String value) {
        // temp
        String newLine = key + " " + value;
        File temp = new File("temp.txt");
        File origin = new File(this.filename);

        try {
            PrintWriter input = new PrintWriter(temp);  //clears file every time
            Scanner sc = new Scanner(origin);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                input.println(line);
            }
            input.println(newLine);
            input.flush();
            input.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        origin.delete();
        temp.renameTo(new File(this.filename));
    }

    private void replaceValue(int key, String value) {
        //temp
        String newLine = key + " " + value;
        File temp = new File("temp.txt");
        File origin = new File(this.filename);

        try {
            PrintWriter input = new PrintWriter(temp);  //clears file every time
            Scanner sc = new Scanner(origin);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                boolean judge = (Integer.parseInt(line.split(" ")[0]) == key);
                if(judge == false) {
                    input.println(line);
                }else {
                    input.println(newLine);
                }
            }
            input.flush();
            input.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        origin.delete();
        temp.renameTo(new File(this.filename));
    }

    private void deleteLine(int key) {
        File temp = new File("temp.txt");
        File origin = new File(this.filename);

        try {
            PrintWriter input = new PrintWriter(temp);  //clears file every time
            Scanner sc = new Scanner(origin);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                boolean judge = (Integer.parseInt(line.split(" ")[0]) == key);
                if(judge == false) {
                    input.println(line);
                }else {
                    // skip this line
                }
            }
            input.flush();
            input.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        origin.delete();
        temp.renameTo(new File(this.filename));
    }

    @Override
    public void run() {
        this.save();
    }

}
