import java.util.*;
import java.util.concurrent.RecursiveAction;

public class mergeSort {
    public static void main(String[] args) {

        //list size
        int size = 1000;

        Random random = new Random();
        ArrayList<Integer> ori_list = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            ori_list.add(random.nextInt(1000));
        }

        ArrayList<Integer> sub_list1 = new ArrayList<Integer>();
        ArrayList<Integer> sub_list2 = new ArrayList<Integer>();

        //break list into equal halves
        for (int i = 0; i < size / 2; i++) {
            sub_list1.add(ori_list.get(i));
        }
        for (int i = size / 2; i < size; i++) {
            sub_list2.add(ori_list.get(i));
        }

        long start_time = System.currentTimeMillis();

        List<sortingThread> thread_list = new ArrayList<>();
        sortingThread sorting_thread1 = new sortingThread(sub_list1);
        sortingThread sorting_thread2 = new sortingThread(sub_list2);

        //Using Comparable interface
        Collections.sort(thread_list);

        //two sorting threads
        sorting_thread1.fork();
        sorting_thread2.fork();
        sorting_thread1.join();
        sorting_thread2.join();

        //merging thread
        mergingThread merger = new mergingThread(sorting_thread1, sorting_thread2);
        merger.fork();
        merger.join();

        long final_time = System.currentTimeMillis() - start_time;

        //System.out.println(merger.get_result());

        System.out.println("Time Used: " + (float) final_time / 1000 + " seconds");
    }
}

class sortingThread extends RecursiveAction implements Comparable<sortingThread> {
    ArrayList<Integer> worker_list;

    @Override
    protected void compute() {
        sorting();
    }

    public ArrayList<Integer> get_worker_list() {
        return worker_list;
    }

    public sortingThread(ArrayList<Integer> list) {
        worker_list = list;
    }

    public void sorting() {
        sublist(0, this.worker_list.size() - 1);
    }

    public void sublist(int first, int last) {
        if (first < last && (last - first) >= 1) {
            int mid = (last + first) / 2;
            sublist(first, mid);
            sublist(mid + 1, last);
            merger(first, mid, last);
        }
    }

    public void merger(int first, int mid, int last) {
        int range = last - first;
        //if less than 100, use insertion sort
        if (range < 100) {
            ArrayList<Integer> temp = new ArrayList<Integer>(insertionSort(worker_list, first, last));
            for (int q = first; q < last; q++) {
                worker_list.set(q, temp.get(q - first));
            }
        } else {
            ArrayList<Integer> merged_sorted_list = new ArrayList<Integer>();

            int left = first;
            int right = mid + 1;

            while (left <= mid && right <= last) {
                if (worker_list.get(left) <= worker_list.get(right)) {
                    merged_sorted_list.add(worker_list.get(left));
                    left++;
                } else {
                    merged_sorted_list.add(worker_list.get(right));
                    right++;
                }
            }

            while (left <= mid) {
                merged_sorted_list.add(worker_list.get(left));
                left++;
            }
            while (right <= last) {
                merged_sorted_list.add(worker_list.get(right));
                right++;
            }

            int i = 0;
            int j = first;
            while (i < merged_sorted_list.size()) {
                worker_list.set(j, merged_sorted_list.get(i));
                i++;
                j++;
            }
        }
    }

    public ArrayList<Integer> insertionSort(ArrayList<Integer> list1, int s, int l) {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (int q = s; q < l; q++) {
            temp.add(list1.get(q));
        }
        for (int j = 1; j < temp.size(); j++) {
            int cur_index = temp.get(j);
            int i = j - 1;
            while ((i > -1) && (temp.get(i) > cur_index)) {
                temp.set(i + 1, temp.get(i));
                i--;
            }
            temp.set(i + 1, cur_index);
        }
        return temp;
    }

    @Override
    public int compareTo(sortingThread o) {
        if (o.get_worker_list().get(0) > worker_list.get(0)) {
            return 1;
        } else if (o.get_worker_list().get(0) == worker_list.get(0)) {
            return 0;
        }
        return -1;
    }

}

class mergingThread extends RecursiveAction {
    sortingThread a;
    sortingThread b;
    ArrayList<Integer> c = new ArrayList<Integer>();

    @Override
    protected void compute() {
        merge();
    }

    mergingThread(sortingThread i, sortingThread j) {
        a = i;
        b = j;
    }

    public ArrayList<Integer> get_result() {
        return c;
    }

    public void merge() {
        while (true) {
            if (a.get_worker_list().size() == 0 && b.get_worker_list().size() == 0) {
                break;
            }
            if (a.get_worker_list().size() == 0) {
                while (b.get_worker_list().size() != 0) {
                    c.add(b.get_worker_list().get(0));
                    b.get_worker_list().remove(0);
                }
            } else if (b.get_worker_list().size() == 0) {
                while (a.get_worker_list().size() != 0) {
                    c.add(a.get_worker_list().get(0));
                    a.get_worker_list().remove(0);
                }

            } else {
                if (a.get_worker_list().get(0) > b.get_worker_list().get(0)) {
                    c.add(b.get_worker_list().get(0));
                    b.get_worker_list().remove(0);
                } else {
                    c.add(a.get_worker_list().get(0));
                    a.get_worker_list().remove(0);
                }
            }
        }
    }
}
