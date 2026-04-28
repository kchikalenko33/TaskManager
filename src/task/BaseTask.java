package task;

 public abstract class BaseTask {
     protected String name;
     protected String description;
     protected int id;
     protected Status status;

     protected  BaseTask (String name, String description, int id, Status status) {
         this.name = name;
         this.description = description;
         this.id = id;
         this.status = status;
     }

     protected BaseTask(String name, String description, Status status) {
         this.status = status;
         this.name = name;
         this.description = description;
     }

     public String getName() {
         return name;
     }

     public void setName(String name) {
         this.name = name;
     }

     public String getDescription() {
         return description;
     }

     public void setDescription(String description) {
         this.description = description;
     }

     public int getId() {
         return id;
     }

     public void setId(int id) {
         this.id = id;
     }

     public Status getStatus() {
         return status;
     }

     public void setStatus(Status status) {
         this.status = status;
     }
     @Override
     public String toString() {
         return String.format("%s{id=%d, name='%s'}",
                 getClass().getSimpleName(), id, name);
     }

 }
