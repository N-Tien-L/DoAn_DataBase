package com.mycompany.quanlythuvien.controller;

/**
 *
 * @author Tien
 */
import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import java.time.LocalDate;
import java.util.Objects;
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

	public boolean markReturned(int idPM, int maBanSao, LocalDate ngayTra, String tinhTrang, String emailNguoiNhan) {
		try {
			if (idPM <= 0 || maBanSao <= 0 || ngayTra == null) return false;
			return dao.markReturned(idPM, maBanSao, ngayTra, tinhTrang, emailNguoiNhan);
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

	// Kiểm tra bản sao có đang bị mượn hay không
	public boolean isMaBanSaoBorrowed(int maBanSao) {
		try {
			if (maBanSao <= 0) return false;
			return dao.isMaBanSaoBorrowed(maBanSao);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Lấy record CT_PM đang mở theo MaBanSao
	public ChiTietPhieuMuon getActiveByMaBanSao(int maBanSao) {
		try {
			if (maBanSao <= 0) return null;
			return dao.getActiveByMaBanSao(maBanSao);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Đếm số sách quá hạn của bạn đọc
	public int countOverdueByBanDoc(int idBD) {
		try {
			if (idBD <= 0) return 0;
			return dao.countOverdueByBanDoc(idBD);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// Số ngày trễ cho 1 CT_PM
	public long getDaysLate(int idPM, int maBanSao) {
		try {
			if (idPM <= 0 || maBanSao <= 0) return 0;
			return dao.getDaysLate(idPM, maBanSao);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}


	// Đếm số sách quá hạn vào một ngày cụ thể
	public int countOverdueOnDate(LocalDate date) {
		try {
			if (Objects.isNull(date)) return 0;
			return dao.countOverdueOnDate(date);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// Paginated CT_PM listing by IdPM with filters
	public com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon> findByIdPMWithFiltersPaginated(int idPM, String returnedFilter, String overdueFilter, int pageIndex, int pageSize) {
		try {
			int total = dao.countByIdPMWithFilters(idPM, returnedFilter, overdueFilter);
			List<ChiTietPhieuMuon> data = dao.findByIdPMWithFiltersPaginated(idPM, returnedFilter, overdueFilter, pageIndex, pageSize);
			return new com.mycompany.quanlythuvien.model.PageResult<>(data, pageIndex, pageSize, total);
		} catch (Exception e) {
			e.printStackTrace();
			return new com.mycompany.quanlythuvien.model.PageResult<>(new ArrayList<>(), pageIndex, pageSize, 0);
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