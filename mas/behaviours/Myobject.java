package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.util.leap.Serializable;

public class Myobject implements Serializable {

    private static final long serialVersionUID = 1L; // Assure compatibility during serialization

    private String id; // An example field
    private String message; // Another example field

    // Constructor
    public Myobject(String id, String message) {
        this.id = id;
        this.message = message;
    }

    // Getter for the id field
    public String getId() {
        return id;
    }

    // Setter for the id field
    public void setId(String id) {
        this.id = id;
    }

    // Getter for the message field
    public String getMessage() {
        return message;
    }

    // Setter for the message field
    public void setMessage(String message) {
        this.message = message;
    }

    // You might want to override the toString() method for easy printing of the object's state
    @Override
    public String toString() {
        return "Myobject{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}