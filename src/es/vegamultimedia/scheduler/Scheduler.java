package es.vegamultimedia.scheduler;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Immutable class that encapsulates a scheduler, and serves as namespace for all
 * the others scheduler-related classes, enums and interfaces.
 * 
 * Use:
 * 
 * Sheduler myScheduler = new Scheduler.Builder().<chain methods>.build(); 
 * new Thread(myScheduler.runnable()).start();
 *  
 * Or:
 * 
 * new Thread(new Scheduler.Builder().<chain methods>.build().runnable()).start();
 * 
 * Immutability guarantees thread-safety for the class itself and most oh its inner
 * classes (all of those that are also immutable). Client code for scheduled tasks,
 * however, must synchronize its access to mutable resources, as usual.     
 * 
 * @author antonio.vera
 */
public class Scheduler {

    public interface Logger {
        void log(String str);
    }
    
    private static Logger logger = new Logger() {
        public void log(String str) {
            System.out.println(str);
            
        }
    };
    
    final private static Constraint.Item[] EMPTY_CONSTRAINT_ITEMS = new Constraint.Item[0];
    final private static Schedulable[] EMPTY_SCHEDULABLES = new Schedulable[0];
    final private static Constraint[] EMPTY_CONSTRAINTS = new Constraint[0];
    final private static long[][] TIME_RANGES = {{0, 59},
                                                 {0, 23},
                                                 {Calendar.SUNDAY, Calendar.SATURDAY},
                                                 {1, 31},
                                                 {Calendar.JANUARY, Calendar.DECEMBER}};
    /**
     * Enumerates time units used in scheduler's time constraints.
     *  
     * @author antonio.vera
     */
    public enum Unit {MINUTE, HOUR, DAY_OF_WEEK, DAY_OF_MONTH, MONTH}
    

    /**
     * Enumerates types of scheduler's time constraints.
     *  
     * @author antonio.vera
     */
    public enum Type {LIST, RANGE}

    final private long lapseTime;
    final private String name;
    final private boolean verbose;
    final private Task[] tasks;

    public static void setLogger(Logger logger) {
        Scheduler.logger = logger;
    }

    private Scheduler(Scheduler.Builder builder) {
        TreeSet<String> taskNames = new TreeSet<String>();
        TreeMap<Integer, String> schInstances = new TreeMap<Integer, String>();
        String schedulerName = builder._name; 
        String taskName;
        int schInsNumber;
        int taskNumber;
        Integer schInsHash;
        verbose = builder._verbose;
        if(!builder._verbose) {
            if(schedulerName==null || schedulerName.isEmpty()) {
                schedulerName = "Unnamed Scheduler";
            }
        } else {
            if(schedulerName==null || schedulerName.isEmpty()) {
                throw new IllegalStateException
                    ("Verbose Schedulers must have a name.");
            }
            logLine(String.format("Building scheduler [%1$s].", schedulerName));
        }
        taskNumber = 1;
        for(Task task: builder._tasks) {
            if(task.name==null || task.name.isEmpty()) {
                taskName = "Task number #" + String.valueOf(taskNumber);
                if(builder._verbose) {
                    throw new IllegalStateException
                        (String.format("In task [%1$s].[%2$s]. All tasks of "
                                + "verbose schedulers must have a name.", 
                                schedulerName, taskName));
                }
            } else {
                taskName = task.name;
            }
            if(taskNames.contains(taskName)) {
                throw new IllegalStateException
                    (String.format("In task [%1$s].[%2$s]. A task with that "
                            + "name already exists.", schedulerName, taskName));
            }
            taskNames.add(taskName);
            schInsNumber = 1;
            for(Schedulable schedulable: task.schedulables) {
                schInsHash = Integer.valueOf(schedulable.hashCode());
                if(schInstances.containsKey(schInsHash)) {
                    throw new IllegalStateException
                        (String.format("Schedulable instance number #%1$s " +
                                       "in Task [%2$s].[%3$s], is already scheduled in " +
                                       "Task [%4$s]",
                                       String.valueOf(schInsNumber), schedulerName,
                                       taskName, schInstances.get(schInsHash)));
                } else {
                    schInstances.put(schInsHash, taskName);
                }
                schInsNumber++;
            }
            taskNumber++;
        }
        lapseTime = builder._lapseTime;
        name = schedulerName;
        // Builder instances are not immutable, so a copy of the tasks list is needed.
        // Also, unnamed tasks need a name, and once task must have a negative interval. 
        tasks = new Task[builder._tasks.size()]; 
        taskNumber = 1;
        for(Task task: builder._tasks) {
            tasks[taskNumber-1] = new Task(task, "Task number #" +
                                           String.valueOf(taskNumber));
            taskNumber++;
        }
    }

    /**
     * Returns a runnable (and mutable) copy/ of the scheduler.
     * @author antonio.vera
     */
    public SchedulerRunnable runnable() {
        return new SchedulerRunnable(this); 
    }
    
    public Thread start() {
        Thread thread = new Thread(runnable());
        thread.start();
        return thread;
    }
    
    /**
     * Starts a method chaining style construction for a immutable Scheduler.Task
     * object.
     *   
     * @return a TaskBuilder object.
     * @author antonio.vera
     */
    public static TaskBuilder task() {
        return new TaskBuilder(); 
    }

    /**
     * Starts a method chaining style construction for a immutable Scheduler.Constraint
     * object.
     *   
     * @return a ConstraintBuilder object.
     * @author antonio.vera
     */
    public static ConstraintBuilder constraint() {
        return new ConstraintBuilder(); 
    }

    /**
     * Starts a method chaining style construction for a immutable Scheduler.Constraint
     * object.
     *   
     * @return a ConstraintBuilder object.
     * @author antonio.vera
     */
    public static ConstraintBuilder constraint(Unit unit, Type type, long... when) {
        return new ConstraintBuilder(unit, type, when); 
    }

    /**
     * Starts a method chaining style construction for a immutable Scheduler.Constraint
     * object.
     *   
     * @return a ConstraintBuilder object.
     * @author antonio.vera
     */
    public static Builder builder() {
        return new Builder(); 
    }

    
    public TestResult test(Calendar from, Calendar to) {
        SchedulerTestable testable = new SchedulerTestable(this);
        testable.setFrom(from);
        testable.setTo(to);
        testable.run();
        return testable.testResult;
    }
    
    public TestResult test(long time) {
        return test(time, TimeUnit.DAYS);
    }

    public TestResult test(long time, TimeUnit timeUnit) {
        Calendar from = GregorianCalendar.getInstance(); 
        Calendar to = GregorianCalendar.getInstance();
        to.setTimeInMillis(from.getTimeInMillis() 
                           + TimeUnit.MILLISECONDS.convert(time, timeUnit));
        return test(from, to);
    }

    private void logLine(String txt) {
        if(verbose) {
            Scheduler.logger.log(txt);
        }
    }


    @SuppressWarnings("unused")
    private void logException(Exception e, Task t) {
        if(t==null) {
            Scheduler.logger.log(String.format("Exception in Scheduler [%1$s]", name));
        } else { 
            Scheduler.logger.log(String.format("Exception in Task [%1$s].[%2$s]", 
                                               name, t.name));
        }
        e.printStackTrace();
    }
    
    /**
     * Dada una lista de Integer's, obtiene un array de int's con el mismo contenido
     * ignorando nulos.
     * @param integers Lista de enteros.
     * @return array de int's.
     * @author antonio.vera
     */
    protected static int[] getIntArray(List<Integer> integers) {
        int i;
        int[] r;
        if(integers==null) { return new int[0]; }
        r = new int[integers.size()]; 
        i = 0;
        for(Integer item: integers) {
            if(item==null) {
                continue;
            }
            r[i] = item.intValue();
            i++;
        }
        return r;
    }
    
    /**
     * Immutable class that encapsulates a task. Scheduler.TaskBuilder objects are
     * the only way to create Scheduler.Task objects. 
     * 
     * @author 
     * antonio.vera
     */
    public static class Task {
        final private boolean once;
        final private long interval;
        final private long delay;
        final private String name;
        final private Schedulable[] schedulables;
        final private Constraint[] constraints;
        
        /**
         * Private constructor. Invoked only from Builder.build(), creates a copy of
         * the task with a default name and a negative interval for run once tasks.     
         * @param task
         * @author antonio.vera
         */
        private Task(Task task, String defaultName) {
            once = task.once;
            interval = (once) ? -1: task.interval;
            delay = task.delay;
            if(task.name!=null && !task.name.isEmpty()) {
                defaultName = task.name;
            }
            name = defaultName; 
            schedulables = task.schedulables;
            constraints = task.constraints;
        }

        /**
         * Private constructor. Invoked only from TaskBuilder.build() 
         * @param taskBuilder
         * @author antonio.vera
         */
        private Task(TaskBuilder taskBuilder) {
            once = taskBuilder._once;
            interval = taskBuilder._interval;
            delay = taskBuilder._delay;
            name = taskBuilder._name;
            // TaskBuilder instances are not immutable, so a copy of the schedulables
            // and constraints lists are needed. 
            schedulables = taskBuilder._schedulables.toArray(EMPTY_SCHEDULABLES);
            constraints = taskBuilder._constraints.toArray(EMPTY_CONSTRAINTS);
        }
        
    }
    
    /**
     * Immutable class that encapsulates a timming constraint for tasks. 
     * Scheduler.ConstraintBuilder objects are the only way to create
     * Scheduler.Constraint objects. 
     * 
     * @author antonio.vera
     */
    public static class Constraint {
        
        final private Item[] constraintItems;
        
        /**
         * Immutable class that encapsulates a timming constraint item for tasks. 
         * 
         * @author antonio.vera
         */
        private static class Item {
            final private Unit unit;
            final private Type type;
            final private long[] when;
            
            /**
             * Private constructor. Invoked only from ConstraintBuilder. 
             * @param builder
             * @author antonio.vera
             */
            private Item(Unit unit, Type type, long... when) {
                this.unit = unit; 
                this.type = type; 
                // A copy of the when array is needed to ensure immutability.
                this.when = when.clone();
            }
        }
        
        /**
         * Private constructor. Invoked only from ConstraintBuilder.build() 
         * @param constraintItems
         * @author antonio.vera
         */
        private Constraint(ConstraintBuilder constraintBuilder) {
            // ConstraintBuilder is not immutable, so a copy of the list is needed
            // to ensure immutability.
            constraintItems = constraintBuilder.items.toArray(EMPTY_CONSTRAINT_ITEMS); 
        }
    }

    /**
     * 
     * 
     * @author antonio.vera
     */
    public static class Builder {
        private long _lapseTime;
        private String _name;
        private boolean _verbose;
        private LinkedList<Task> _tasks;
        
        public Builder() {
            _lapseTime = 60*1000;
            _verbose = false;
            _name = null;
            _tasks = new LinkedList<Task>();
        }

        public Builder lapse(long v) {
            return lapse(v, TimeUnit.SECONDS);
        }
        
        public Builder lapse(long v, TimeUnit tu) {
            _lapseTime = TimeUnit.MILLISECONDS.convert(v, tu);
            if(_lapseTime<=0) {
                throw new IllegalArgumentException
                    (String.format("Task intervals must be greater than zero."));
            }
            return this;
        }

        public Builder name(String v) {
            _name = v;
            return this;
        }

        public Builder verbose(boolean v) {
            _verbose = v;
            return this;
        }
        
        public Builder add(Task v) {
            return task(v);
        }

        public Builder task(Task v) {
            _tasks.add(v);
            return this;
            
        }
        
        public Scheduler build() {
            return new Scheduler(this);
        }
    }

    /**
     * Class that encapsulates a runnable (and mutable) version of a Scheduler
     * instance. 
     * 
     * Because this is a mutable class, you shouldn't keep references for objects of
     * that type, and use it only in the Thread creation. e.g.:
     * 
     * Thread myThread = new Thread(myScheduler.runnable());
     * 
     * @author antonio.vera
     */
    public static class SchedulerRunnable implements Runnable {

        final protected long lapseTime;
        final protected boolean verbose;
        final protected String name;
        final protected Task[] tasks;

        private TaskState[] taskStates;
        long[] lastTimming;
        
        private SchedulerRunnable(Scheduler scheduler) {
            lapseTime = scheduler.lapseTime;
            verbose = scheduler.verbose;
            name = scheduler.name;
            tasks = scheduler.tasks;
            taskStates = new TaskState[tasks.length];
            lastTimming = new long[] {-1, -1, 0, 0, 0};
            for(int i=0; i<tasks.length; i++) {
                taskStates[i] = new TaskState();
            }
        }
        
        public void run() {
            Calendar ct;
            long[] currentTimming = new long[Scheduler.TIME_RANGES.length];
            long currentMS;
            try {
                logLine("Vega Multimedia Scheduler bootting up...");
                for(Task task: tasks) {
                    loadTask(task);
                }
                while(true) {
                    TimeUnit.MILLISECONDS.sleep(lapseTime);
                    ct = getCurrentTime();
                    currentTimming[0] = ct.get(Calendar.MINUTE);
                    currentTimming[1] = ct.get(Calendar.HOUR_OF_DAY);
                    currentTimming[2] = ct.get(Calendar.DAY_OF_WEEK);
                    currentTimming[3] = ct.get(Calendar.DAY_OF_MONTH);
                    currentTimming[4] = ct.get(Calendar.MONTH);
                    currentMS = ct.getTimeInMillis();
                    runOnce(currentTimming, currentMS);
                }
            } catch(InterruptedException e) {
                logLine("Vega Multimedia Scheduler (friendly) shutting down...");
            } catch(Exception e) {
                e.printStackTrace();
                logLine("Vega Multimedia Scheduler (unfriendly) shutting down...");
            }
            endRun();
            for(Task task: tasks) {
                unloadTask(task);
            }
        }

        private boolean timmingChanged(long[] currentTimming) {
            int i;
            i = 0;
            while(i<lastTimming.length) {
                if(lastTimming[i]!=currentTimming[i]) {
                    do {
                        lastTimming[i]=currentTimming[i];
                        i++;
                    } while(i<lastTimming.length);
                    return true;
                }
                i++;
            }
            return false;
        }
        
        protected void runOnce(long[] currentTimming, long currentMS) {
            TaskState taskState;
            Task task;
            long diffMS;
            int t, tt;
            tt = tasks.length;
            if(timmingChanged(currentTimming)) {
                Constraint constraint;
                boolean status;
                int c, cc;
                for(t=0; t<tt; t++) {
                    task = tasks[t];
                    cc = task.constraints.length;
                    status = (cc==0) ? true : false;
                    for(c=0; c<cc; c++) {
                        constraint = task.constraints[c]; 
                        if(constraintsMatch(constraint, currentTimming)) {
                            status = true;
                            break;
                        }
                    }
                    taskStates[t].lastStatus = taskStates[t].status;
                    taskStates[t].status = status; 
                }
            }
            for(t=0; t<tt; t++) {
                task = tasks[t];
                taskState = taskStates[t];
                if(taskState.status) {
                    if(!taskState.lastStatus) {
                        awakeTask(task);
                        taskState.lastMS = currentMS;
                        taskState.nextRunMS = task.delay;
                    }
                    if(taskState.nextRunMS>=0) {
                        diffMS = currentMS - taskState.lastMS;
                        if(diffMS>=taskState.nextRunMS) {
                            runTask(task);
                            taskState.lastMS += taskState.nextRunMS;
                            taskState.nextRunMS = task.interval;
                            if(taskState.nextRunMS<0) {
                                sleepTask(task);
                            }
                        }
                    }
                }  else {
                    if(taskState.lastStatus) {
                        if(taskState.nextRunMS>=0) {
                            sleepTask(task);
                        }
                    }
                }
                taskStates[t].lastStatus = taskState.status; 
            }
        }

        protected void endRun() {
            TaskState taskState;
            Task task;
            int t, tt;
            tt = tasks.length;
            for(t=0; t<tt; t++) {
                task = tasks[t];
                taskState = taskStates[t];
                if(taskState.lastStatus) {
                    taskState.lastStatus = false;
                    if(taskState.nextRunMS>=0) {
                        sleepTask(task);
                    }
                }
            }
        }

        private boolean constraintsMatch(Constraint constraint, long[] currentTimming) {
            boolean match;
            int i;
            long r1, r2, ct;
            int unitIndex;
            long[] range;
            
            for(Constraint.Item ci: constraint.constraintItems) {
                unitIndex = ci.unit.ordinal();
                ct = currentTimming[unitIndex];
                range = Scheduler.TIME_RANGES[unitIndex];
                match = false;
                switch(ci.type) {
                case LIST:
                    for(i=0; i<ci.when.length; i++) {
                        if(ct==ci.when[i]){
                            match = true;
                            break;
                        }
                    }
                    break;
                case RANGE:
                    for(i=0; i<ci.when.length; i+=2) {
                        r1 = ci.when[i];
                        r2 = ci.when[i+1];
                        if(r2<r1) {
                            r2 += range[1] + 1 - range[0];
                        }
                        if(ct<r1) {
                            ct += range[1] + 1 - range[0];
                        }
                        if(ct<=r2) {
                            match = true;
                            break;
                        }
                    }
                    break;
                }
                if(match==false) {
                    return false;
                }
            }
            return true;
        }
        
        private void logTask(Task t, String append) {
            if(verbose) {
                logLine(String.format("Scheduler task [%1$s].[%2$s]%3$s", name, t.name, append));
            }
        }
        
        protected void loadTask(Task t) {
            logTask(t, " loading");
            for(Schedulable schedulable: t.schedulables) {
                try {
                    schedulable.load();
                } catch(Exception e) {
                    logException(e, t);
                }
            }
        }

        protected void unloadTask(Task t) {
            logTask(t, " unloading");
            for(Schedulable schedulable: t.schedulables) {
                try {
                    schedulable.unload();
                } catch(Exception e) {
                    logException(e, t);
                }
            }
        }

        protected void awakeTask(Task t) {
            logTask(t, " awaking");
            for(Schedulable schedulable: t.schedulables) {
                try {
                    schedulable.awake();
                } catch(Exception e) {
                    logException(e, t);
                }
            }
        }

        protected void sleepTask(Task t) {
            logTask(t, " sleeping");
            for(Schedulable schedulable: t.schedulables) {
                try {
                    schedulable.sleep();
                } catch(Exception e) {
                    logException(e, t);
                }
            }
        }
        
        protected void runTask(Task t) {
            logTask(t, " running");
            for(Schedulable schedulable: t.schedulables) {
                try {
                    schedulable.run();
                } catch(Exception e) {
                    logException(e, t);
                }
            }
        }

        private Calendar getCurrentTime() {
            return GregorianCalendar.getInstance();
        }

        private void logException(Exception e, Task t) {
            if(t==null) {
                Scheduler.logger.log(String.format("Exception in Scheduler [%1$s]", 
                                                   name));
            } else { 
                Scheduler.logger.log(String.format("Exception in Task [%1$s].[%2$s]", 
                                                   name, t.name));
            }
            e.printStackTrace();
        }

        private void logLine(String txt) {
            if(verbose) {
                Scheduler.logger.log(txt);
            }
        }

    }
    
    private static class TaskState {
        private boolean status = false;
        private boolean lastStatus = false;
        private long lastMS;
        private long nextRunMS;
    }
    
    public static class TestResult {
        TreeMap<Integer, LinkedList<TestResultItem>> result =
            new TreeMap<Integer, LinkedList<TestResultItem>>();
        TreeMap<Integer, String> tasksNames = new TreeMap<Integer, String>();
        
        public String toString() {
            boolean newLine = false;
            StringBuilder str = new StringBuilder("");
            for(Entry<Integer, LinkedList<TestResultItem>> entry: result.entrySet()) {
                entry.getKey();
                if(newLine) {
                    str.append("\r\n");
                }
                str.append("[");
                str.append(tasksNames.get(entry.getKey().intValue()));
                str.append("]: ");
                for(TestResultItem item: entry.getValue()) {
                    if(item.type.equals(TestResultItem.Type.AWAKE) ||
                       item.type.equals(TestResultItem.Type.UNLOAD)) {
                        str.append(" ");
                    }
                    str.append(item.type.toString().substring(0,1));
                    if(item.count>1) {
                        str.append("(");
                        str.append(item.count);
                        str.append(")");
                    }
                }
                newLine = true;
            }
            return str.toString();
        }

        public String explain() {
            Format dateFormat = new SimpleDateFormat("EE, yyyy/MMM/dd, HH:mm:ss.SSS");
            Calendar cal = GregorianCalendar.getInstance();
            StringBuilder str = new StringBuilder("");
            String tab1 = "  "; 
            String tab2 = "    "; 
            String tab3 = "      ";
            String tab = "";
            
            for(Entry<Integer, LinkedList<TestResultItem>> entry: result.entrySet()) {
                entry.getKey();
                str.append("[");
                str.append(tasksNames.get(entry.getKey().intValue()));
                str.append("]:\r\n");
                for(TestResultItem item: entry.getValue()) {
                    switch(item.type) {
                    case LOAD:  case UNLOAD:  tab = tab1; break;
                    case AWAKE: case SLEEP:   tab = tab2; break;
                    case RUN: tab = tab3; break;
                    }
                    str.append(tab);
                    str.append(item.type.toString());
                    if(item.count>1) {
                        str.append("(");
                        str.append(item.count);
                        str.append(" times) at ");
                        
                        cal.setTimeInMillis(item.time);
                        str.append(dateFormat.format(cal.getTime()));
                        str.append(" ... ");
                                
                        cal.setTimeInMillis(item.time2);
                        str.append(dateFormat.format(cal.getTime()));
                        
                    } else {
                        cal.setTimeInMillis(item.time);
                        str.append(" at ");
                        str.append(dateFormat.format(cal.getTime()));
                    }
                    str.append("\r\n");
                }
                str.append("\r\n");
            }
            return str.toString();
        }
    }

    public static class TestResultItem {
    
        public enum Type {LOAD, UNLOAD, AWAKE, SLEEP, RUN};
        
        TestResultItem.Type type;
        long time;
        long time2;
        int count;
    
        private TestResultItem(TestResultItem.Type type, long time) {
            this.type = type;
            this.time = time;
            this.time2 = time;
            this.count = 1;
        }
    }
    
    private static class SchedulerTestable extends SchedulerRunnable {
        Calendar from;
        Calendar to;
        long currentMS;
        TestResult testResult = new TestResult();
        
        private SchedulerTestable(Scheduler scheduler) {
            super(scheduler);
            from = GregorianCalendar.getInstance();
            to = GregorianCalendar.getInstance();
            to.setTimeInMillis(from.getTimeInMillis() + 24*60*60*1000);
        }

        private void setFrom(Calendar from) {
            this.from.setTimeInMillis(from.getTimeInMillis());
        }
        
        private void setTo(Calendar to) {
            this.to.setTimeInMillis(to.getTimeInMillis());
        }
        
        
        private void addTestResult(Task task, TestResultItem.Type type) {
            Integer taskHash = Integer.valueOf(task.hashCode());
            LinkedList<TestResultItem> testResultItems; 
            TestResultItem it;

            if(!testResult.result.containsKey(taskHash)) {
                testResult.result.put(taskHash, new LinkedList<TestResultItem>());
                testResult.tasksNames.put(taskHash, task.name);
            }
            testResultItems = testResult.result.get(taskHash);
            int ii;
            ii = testResultItems.size();
            if(ii>0) {
                it = testResultItems.get(ii-1);
                if(it.type==type) {
                    it.count++;
                    it.time2 = currentMS;
                    return;
                }
            }
            testResultItems.add(new TestResultItem(type, currentMS));
        }
        
        public void run() {
            Calendar ct = GregorianCalendar.getInstance();
            currentMS = from.getTimeInMillis();
            ct.setTimeInMillis(currentMS);
            long[] currentTimming = new long[Scheduler.TIME_RANGES.length];
            long endMS = to.getTimeInMillis();
            try {
                for(Task task: tasks) {
                    loadTask(task);
                }
                while(true) {
                    currentTimming[0] = ct.get(Calendar.MINUTE);
                    currentTimming[1] = ct.get(Calendar.HOUR_OF_DAY);
                    currentTimming[2] = ct.get(Calendar.DAY_OF_WEEK);
                    currentTimming[3] = ct.get(Calendar.DAY_OF_MONTH);
                    currentTimming[4] = ct.get(Calendar.MONTH);
                    currentMS = ct.getTimeInMillis();
                    runOnce(currentTimming, currentMS);
                    currentMS += lapseTime;
                    if(currentMS>endMS) {
                        break;
                    }
                    ct.setTimeInMillis(currentMS + lapseTime);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            endRun();
            for(Task task: tasks) {
                
                unloadTask(task);
            }
        }
        
        protected void loadTask(Task t) {
            addTestResult(t, TestResultItem.Type.LOAD);
        }

        protected void unloadTask(Task t) {
            addTestResult(t, TestResultItem.Type.UNLOAD);
        }

        protected void awakeTask(Task t) {
            addTestResult(t, TestResultItem.Type.AWAKE);
        }

        protected void sleepTask(Task t) {
            addTestResult(t, TestResultItem.Type.SLEEP);
        }
        
        protected void runTask(Task t) {
            addTestResult(t, TestResultItem.Type.RUN);
        }

        
    }

    /**
     * Buider for Scheduler.Task objects. Static method Scheduler.task() is the only
     * way to create TaskBuilder objects.
     * 
     * TaskBuilder is not an immutable class, keep that in mind if you share an
     * instance of TaskBuilder among threads (but I can't imagine why anybody could
     * need to do such a thing).
     * 
     * @author antonio.vera
     */
    public static class TaskBuilder {
        private boolean _once;
        private long _interval;
        private long _delay;
        private String _name;
        private LinkedList<Schedulable> _schedulables;
        private LinkedList<Constraint> _constraints;
        
        private TaskBuilder()  {
            _once = false;
            _interval = 60*60*1000;
            _delay = 0;
            _schedulables = new LinkedList<Schedulable>();
            _constraints = new LinkedList<Constraint>(); 
        }
        
        public TaskBuilder once(boolean v) {
            _once = v; 
            return this; 
        }

        public TaskBuilder interval(long v) {
            return interval(v, TimeUnit.SECONDS); 
        }

        public TaskBuilder interval(long v, TimeUnit tu) {
            _interval = TimeUnit.MILLISECONDS.convert(v, tu);
            if(_interval<=0) {
                throw new IllegalArgumentException
                    (String.format("Task intervals must be greater than zero."));
                
            }
            return this; 
        }
        
        public TaskBuilder delay(long v) {
            return delay(v, TimeUnit.SECONDS); 
        }

        public TaskBuilder delay(long v, TimeUnit tu) {
            _delay = TimeUnit.MILLISECONDS.convert(v, tu);
            if(_delay<0) {
                throw new IllegalArgumentException
                    (String.format("Task intervals must be a non negative number."));
                
            }
            return this; 
        }

        public TaskBuilder name(String v) {
            _name = v; 
            return this; 
        }

        public TaskBuilder constraint(Constraint v) {
            _constraints.add(v);
            return this; 
        }

        public TaskBuilder constraint(ConstraintBuilder v) {
            return constraint(v.build());
        }
        
        /**
         * Adds a Schedulable instance to the Task.
         * 
         * Multiple calls for the same task are allowed. It's legal schedule more
         * than one Schedulable object in the same task (but not schedule the same
         * Schedulable instance in more than one Task).
         * 
         * @author antonio.vera
         */
        public TaskBuilder schedule(Schedulable schedulable) {
            _schedulables.add(schedulable);
            return this;
        }
        
        public Task build() {
            return new Task(this);
        }
    }

    
    
    /**
     * Buider for Scheduler.Constraint objects. Static method Scheduler.constraint(),
     * and <ConstraintBuilder Instance>.and() are the only ways to create
     * ConstraintBuilder objects.
     * 
     * ConstraintBuilder is not an immutable class, keep that in mind if you share an
     * instance of ConstraintBuilder among threads (but I can't imagine why anybody
     * could need to do such a thing).
     * 
     * @author antonio.vera
     */
    public static class ConstraintBuilder {
        private LinkedList<Constraint.Item> items;
        
        /**
         * Private constructor. Invoked only from static Scheduler.constraint() and
         * <ConstraintBuilder Instance>.and().
         * @param constraintItems
         * @author antonio.vera
         */
        private ConstraintBuilder()  {
            items = new LinkedList<Constraint.Item>();
        }

        /**
         * Private constructor. Invoked only from static Scheduler.constraint() and
         * @author antonio.vera
         */
        private ConstraintBuilder(Unit unit, Type type, long... when) {
            this();
            items.add(new Constraint.Item(unit, type, when));
        }
        
        /**
         * Add a new Constraint.Item to the constraint.
         * @author antonio.vera
         */
        public ConstraintBuilder add(Unit unit, Type type, long... when) {
            int ii, unitIndex;
            ii = when.length;
            long[] range;
            if(ii==0) {
                throw new IllegalArgumentException
                    ("Constraints must have at least a timming.");
            }
            if(type==Type.RANGE) {
                if((ii%2)==1) {
                    throw new IllegalArgumentException
                        ("Range constraints must have an even number of timmings.");
                }
            }
            unitIndex = unit.ordinal();
            range = Scheduler.TIME_RANGES[unitIndex];
            for(int i=0; i<ii; i++) {
                if( (when[i]>=range[0]) &&
                    (when[i]<=range[1]) ) {
                    continue;
                }
                if(unit.equals(Unit.DAY_OF_WEEK)) {
                    throw new IllegalArgumentException
                        ("DAY_OF_WEEK constraints timmings must be numbers "
                         + "between Calendar.SUNDAY and Calendar.SATURDAY.");
                } else if(unit.equals(Unit.MONTH)) {
                    throw new IllegalArgumentException
                        ("MONTH constraints timmings must be numbers "
                         + "between Calendar.JANUARY and Calendar.DECEMBER.");
                } else {
                    throw new IllegalArgumentException
                        (String.format("%1$s constraints timmings must numbers "
                                + "between %2$s and %3$s.",
                                unit.toString(),range[0], range[1]));
                }
            }
            items.add(new Constraint.Item(unit, type, when));
            return this;
        }

        /**
         * Build the Scheduler.Constraint instance. 
         * @return
         * @author antonio.vera
         */
        public Constraint build()  {
            return new Constraint(this);
        }
        
    }

    /**
     * A class that implements this interface, can perform scheduled tasks if
     * is attached to a Scheduler instance.
     * 
     * @author antonio.vera
     */
    public interface Schedulable {
        
        /**
         * Main entry point for schedulable tasks.
         * 
         * If the task is a run-once task, the scheduler invoques this method JUST
         * ONCE EVERY TIME its status changes from false to true.
         *  
         * If the task is an interval task, the scheduler invoques this method ONCE
         * JUST WHEN its status changes from false to true and and AGAIN AND AGAIN
         * AFTER EVERY interval as long as his status remains true.
         * 
         * @author antonio.vera
         */
        public void run();
        

        /**
         * Called just after a scheduler is bootted up, regardless of the schedulable
         * task status.
         * 
         * @author antonio.vera
         */
        public void load();

        
        /**
         * Called when a scheduler is shutting down, regardless of the schedulable
         * task status.
         * 
         * @author antonio.vera
         */
        public void unload();
        
        
        /**
         * Called ONCE when the schedulable task status changes over time and goes from
         * true to false.
         * 
         * @author antonio.vera
         */
        public void sleep();

        
        /**
         * Called ONCE when the schedulable task status changes over time and goes from
         * false to true.  
         * 
         * @author antonio.vera
         */
        public void awake();
    }
}
