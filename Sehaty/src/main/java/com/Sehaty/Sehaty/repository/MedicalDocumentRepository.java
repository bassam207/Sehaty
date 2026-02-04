package com.Sehaty.Sehaty.repository;

import com.Sehaty.Sehaty.document.MedicalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalDocumentRepository extends MongoRepository<MedicalDocument, String> {
}
