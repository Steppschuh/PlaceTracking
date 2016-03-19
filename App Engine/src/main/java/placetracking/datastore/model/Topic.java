package placetracking.datastore.model;

import placetracking.datastore.OfyHelper;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The @Entity tells Objectify about our entity.  We also register it in {@link OfyHelper}
 * Our primary key @Id is set automatically by the Google Datastore for us.
 *
 * Objectify, unlike the AppEngine library requires that you specify the fields you
 * want to index using @Index.  Only indexing the fields you need can lead to substantial gains in
 * performance -- though if not indexing your data from the start will require indexing it later.
 *
 * NOTE - all the properties are PUBLIC so that can keep the code simple.
 **/

@Entity
public class Topic {

    @Id public Long id;

    @Index public String name;

    public Topic() {
    }

    /**
     * A convenience constructor
     **/
    public Topic(String name) {
        this();
        this.name = name;
    }

}