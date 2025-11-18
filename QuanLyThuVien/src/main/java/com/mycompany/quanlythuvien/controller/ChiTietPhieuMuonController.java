package com.mycompany.quanlythuvien.controller;

/**
 *
 * @author Tien
 */
import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuMuonController {
	private final ChiTietPhieuMuonDAO dao = new ChiTietPhieuMuonDAO();

	public List<ChiTietPhieuMuon> getAll() {
		try {
			return dao.getAll();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<ChiTietPhieuMuon> getOverDue() {
		try {
			return dao.getOverDue();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<ChiTietPhieuMuon> findByBandocAndStatus(int idBD, boolean isReturned) {
		try {
			if (idBD <= 0) return new ArrayList<>();
			return dao.findByBandocAndStatus(idBD, isReturned);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public void insert(ChiTietPhieuMuon ctp) throws Exception {
		validate(ctp, true);
		boolean ok = dao.createNew(ctp);
		if (!ok) throw new Exception("Tạo chi tiết phiếu mượn thất bại");
	}

	public boolean markReturned(int idPM, int maBanSao, LocalDate ngayTra) {
		try {
			if (idPM <= 0 || maBanSao <= 0 || ngayTra == null) return false;
			return dao.markReturned(idPM, maBanSao, ngayTra);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean markReturnedBatch(int idPM, List<Integer> maBanSaoList, LocalDate ngayTra) {
		try {
			if (idPM <= 0 || maBanSaoList == null || maBanSaoList.isEmpty() || ngayTra == null) return false;
			return dao.markReturnedBatch(idPM, maBanSaoList, ngayTra);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean delete(int idPM, int maBanSao) {
		try {
			if (idPM <= 0 || maBanSao <= 0) return false;
			return dao.delete(idPM, maBanSao);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<ChiTietPhieuMuon> findByIdPM(int idPM) {
		try {
			if (idPM <= 0) return new ArrayList<>();
			return dao.findByIdPM(idPM);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<ChiTietPhieuMuon> findUnreturnedByIdPM(int idPM) {
		try {
			if (idPM <= 0) return new ArrayList<>();
			return dao.findUnreturnedByIdPM(idPM);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public ChiTietPhieuMuon findByMaBanSao(int maBanSao) {
		try {
			if (maBanSao <= 0) return null;
			return dao.findByMaBanSao(maBanSao);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void validate(ChiTietPhieuMuon ctp, boolean isNew) throws Exception {
		if (ctp == null) throw new Exception("Dữ liệu chi tiết phiếu mượn không hợp lệ");

		if (ctp.getIdPM() <= 0) throw new Exception("Id phiếu mượn không hợp lệ");
		if (ctp.getMaBanSao() <= 0) throw new Exception("Mã bản sao không hợp lệ");

		// Actual pay date can be null when unpaid; when inserting, null can be allowed
		if (ctp.getNgayTraThucTe() != null && ctp.getNgayTraThucTe().isAfter(LocalDate.now()))
			throw new Exception("Ngày trả thực tế không thể là tương lai");
	}
}
