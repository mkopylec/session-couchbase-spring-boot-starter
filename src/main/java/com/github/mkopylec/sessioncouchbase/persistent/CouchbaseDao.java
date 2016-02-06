package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.data.couchbase.repository.CouchbaseRepository;

public interface CouchbaseDao extends CouchbaseRepository<SessionEntity, String> {

}
