package ru.psu.shells.ess.components;

import ru.psu.shells.ess.model.entity.Domain;
import ru.psu.shells.ess.model.entity.Rule;
import ru.psu.shells.ess.model.entity.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Storage {
    private static ArrayList<Rule> rules = new ArrayList<>();
    private static ArrayList<Variable> variables = new ArrayList<>();
    private static ArrayList<Domain> domains = new ArrayList<>();

    public static void save(String path) {
        HashMap<String, ArrayList> hashMap = new HashMap<>();
        hashMap.put("rules", rules);
        hashMap.put("variables", variables);
        hashMap.put("domains", domains);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(hashMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            HashMap<String, ArrayList> hashMap = (HashMap<String, ArrayList>) ois.readObject();
            domains = (ArrayList<Domain>) hashMap.get("domains");
            variables = (ArrayList<Variable>) hashMap.get("variables");
            rules = (ArrayList<Rule>) hashMap.get("rules");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Rule> getRules() {
        return rules;
    }

    public static ArrayList<Variable> getVariables() {
        return variables;
    }

    public static ArrayList<Domain> getDomains() {
        return domains;
    }

    public static void clear() {
        rules.clear();
        variables.clear();
        domains.clear();
    }
}
