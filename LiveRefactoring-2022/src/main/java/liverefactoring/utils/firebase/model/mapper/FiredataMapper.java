package liverefactoring.utils.firebase.model.mapper;


import com.google.firebase.database.DataSnapshot;
import liverefactoring.utils.firebase.model.FireNode;

import java.util.Iterator;

public class FiredataMapper {

    FireNode mapFromFirebase(DataSnapshot snapshot) {
        Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
        // construct the root object
        DataSnapshot rootSnapshot = it.next();
        FireNode root = new FireNode(rootSnapshot.getKey());
        for (DataSnapshot c : rootSnapshot.getChildren()) {
            // child object
            FireNode child = new FireNode(c.getKey());
            root.addChild(child);
        }
        while (it.hasNext()) {

        }
        return new FireNode("");

    }

}
