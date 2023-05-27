package com.invoice .api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.invoice.api.entity.Invoice;

@Repository
public interface RepoInvoice extends JpaRepository<Invoice, Integer>{
	
	@Query(value="SELECT * FROM invoice WHERE rfc = :rfc AND status = :status", nativeQuery=true)
	List<Invoice> findByRfcAndStatus(String rfc, Integer status);

}
