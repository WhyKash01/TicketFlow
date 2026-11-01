package com.ticketflow.booking.algorithm;

/**
 * Segment Tree implementation for contiguous available seat allocation.
 * Tracks available seats (1) and unavailable/locked/booked seats (0).
 * Queries optimal contiguous blocks of K seats in O(log N) time.
 */
public class SegmentTree {

    public static class Node {
        public int length;
        public int maxContiguous;
        public int prefixContiguous;
        public int suffixContiguous;
        public int startIndex; // Starting index in the original array of the best contiguous block

        public Node(int length, int val, int index) {
            this.length = length;
            this.maxContiguous = (val == 1) ? 1 : 0;
            this.prefixContiguous = (val == 1) ? 1 : 0;
            this.suffixContiguous = (val == 1) ? 1 : 0;
            this.startIndex = (val == 1) ? index : -1;
        }

        public Node() {
            this.length = 0;
            this.maxContiguous = 0;
            this.prefixContiguous = 0;
            this.suffixContiguous = 0;
            this.startIndex = -1;
        }
    }

    private final int size;
    private final Node[] tree;
    private final int[] original;

    public SegmentTree(int[] seatsAvailable) {
        this.size = seatsAvailable.length;
        this.original = seatsAvailable.clone();
        this.tree = new Node[4 * size + 1];
        if (size > 0) {
            build(1, 0, size - 1);
        }
    }

    private Node merge(Node left, Node right, int mid) {
        Node parent = new Node();
        parent.length = left.length + right.length;

        // Prefix
        if (left.prefixContiguous == left.length) {
            parent.prefixContiguous = left.length + right.prefixContiguous;
        } else {
            parent.prefixContiguous = left.prefixContiguous;
        }

        // Suffix
        if (right.suffixContiguous == right.length) {
            parent.suffixContiguous = right.length + left.suffixContiguous;
        } else {
            parent.suffixContiguous = right.suffixContiguous;
        }

        // Middle cross-boundary contiguous span
        int cross = left.suffixContiguous + right.prefixContiguous;
        int crossStart = (mid - left.suffixContiguous + 1);

        // Find max among left.max, right.max, and cross
        if (left.maxContiguous >= right.maxContiguous && left.maxContiguous >= cross) {
            parent.maxContiguous = left.maxContiguous;
            parent.startIndex = left.startIndex;
        } else if (cross >= right.maxContiguous) {
            parent.maxContiguous = cross;
            parent.startIndex = crossStart;
        } else {
            parent.maxContiguous = right.maxContiguous;
            parent.startIndex = right.startIndex;
        }

        return parent;
    }

    private void build(int nodeIdx, int start, int end) {
        if (start == end) {
            tree[nodeIdx] = new Node(1, original[start], start);
            return;
        }
        int mid = (start + end) / 2;
        int leftChild = 2 * nodeIdx;
        int rightChild = 2 * nodeIdx + 1;

        build(leftChild, start, mid);
        build(rightChild, mid + 1, end);

        tree[nodeIdx] = merge(tree[leftChild], tree[rightChild], mid);
    }

    public void update(int index, int val) {
        if (index < 0 || index >= size) return;
        original[index] = val;
        update(1, 0, size - 1, index, val);
    }

    private void update(int nodeIdx, int start, int end, int index, int val) {
        if (start == end) {
            tree[nodeIdx] = new Node(1, val, start);
            return;
        }
        int mid = (start + end) / 2;
        int leftChild = 2 * nodeIdx;
        int rightChild = 2 * nodeIdx + 1;

        if (index <= mid) {
            update(leftChild, start, mid, index, val);
        } else {
            update(rightChild, mid + 1, end, index, val);
        }

        tree[nodeIdx] = merge(tree[leftChild], tree[rightChild], mid);
    }

    /**
     * Finds the start index of the first available block of 'k' contiguous seats.
     * Returns -1 if no contiguous block of size k is available.
     */
    public int findContiguous(int k) {
        if (k <= 0 || k > size || tree[1] == null || tree[1].maxContiguous < k) {
            return -1;
        }
        return findFirst(1, 0, size - 1, k);
    }

    private int findFirst(int nodeIdx, int start, int end, int k) {
        if (start == end) {
            return (tree[nodeIdx].maxContiguous >= k) ? start : -1;
        }

        int mid = (start + end) / 2;
        int leftChild = 2 * nodeIdx;
        int rightChild = 2 * nodeIdx + 1;

        // Check if left subtree can satisfy
        if (tree[leftChild] != null && tree[leftChild].maxContiguous >= k) {
            return findFirst(leftChild, start, mid, k);
        }

        // Check if cross-boundary contiguous span satisfies
        int cross = (tree[leftChild] != null ? tree[leftChild].suffixContiguous : 0)
                + (tree[rightChild] != null ? tree[rightChild].prefixContiguous : 0);
        if (cross >= k) {
            int leftSuffix = tree[leftChild] != null ? tree[leftChild].suffixContiguous : 0;
            return mid - leftSuffix + 1;
        }

        // Check right subtree
        if (tree[rightChild] != null && tree[rightChild].maxContiguous >= k) {
            return findFirst(rightChild, mid + 1, end, k);
        }

        return -1;
    }

    public int getMaxContiguous() {
        return (tree[1] != null) ? tree[1].maxContiguous : 0;
    }
}

