package com.mycompany.app;

/**
 * A circular linked list implementation designed for the purpose of tracking player turns.
 * Player ids are stored as nodes with the tail node always pointing back to the head node.
 */
public class TurnList {

    Node head, tail;
    int size;

    private class Node {
        int id;
        Node next;

        Node(int id, Node next) {
            this.id = id;
            this.next = next;
        }

    }

    TurnList() {
        head = null;
        tail = null;
        size = 0;
    }

    public void add(int id) {
        Node newNode = new Node(id, head);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
    }

    public void printList() {
        Node current = head;

        if (current == null) {
            System.out.println();
            return;
        }

        System.out.print(current.id + " ");
        while (current.next != head) {
            current = current.next;
            System.out.print(current.id + " ");
        }

        System.out.println();
    }

}
