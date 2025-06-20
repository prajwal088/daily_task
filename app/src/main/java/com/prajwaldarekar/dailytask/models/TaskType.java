package com.prajwaldarekar.dailytask.models;

public enum TaskType {
    TASK {
        @Override
        public String toString() {
            return "Task";
        }
    },
    NOTE {
        @Override
        public String toString() {
            return "Note";
        }
    },
    REMINDER {
        @Override
        public String toString() {
            return "Reminder";
        }
    }
}
