package service;

import dao.ReviewDAO;
import dao.TurfDAO;
import dao.UserDAO;
import model.Admin;
import model.Turf;
import model.User;

import java.util.List;

/** Admin-only actions. Every method first checks that the caller really is an Admin. */
public class AdminService {

    private final UserDAO userDAO = new UserDAO();
    private final TurfDAO turfDAO = new TurfDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    public List<User> listUsers(User caller) {
        requireAdmin(caller);
        return userDAO.findAll();
    }

    /** Deletes a user and (by CASCADE) their turfs, bookings and reviews. Admins cannot delete themselves. */
    public void removeUser(User caller, int userId) {
        requireAdmin(caller);
        if (caller.getUserId() == userId) throw new IllegalArgumentException("You cannot remove your own account");
        if (!userDAO.delete(userId)) throw new IllegalArgumentException("User not found");
    }

    public List<Turf> getPendingTurfs(User caller) {
        requireAdmin(caller);
        return turfDAO.findPending();
    }

    public void approveTurf(User caller, int turfId) {
        requireAdmin(caller);
        turfDAO.findById(turfId).orElseThrow(() -> new IllegalArgumentException("Turf not found"));
        turfDAO.setApproved(turfId, true);
    }

    /** Rejecting deletes a turf that was never approved. Approved turfs are not removed here. */
    public void rejectTurf(User caller, int turfId) {
        requireAdmin(caller);
        Turf t = turfDAO.findById(turfId).orElseThrow(() -> new IllegalArgumentException("Turf not found"));
        if (t.isApproved()) throw new IllegalStateException("This turf is already approved");
        turfDAO.delete(turfId);
    }

    public void removeReview(User caller, int reviewId) {
        requireAdmin(caller);
        if (!reviewDAO.delete(reviewId)) throw new IllegalArgumentException("Review not found");
    }

    private void requireAdmin(User caller) {
        if (!(caller instanceof Admin)) throw new SecurityException("Admin access required");
    }
}
