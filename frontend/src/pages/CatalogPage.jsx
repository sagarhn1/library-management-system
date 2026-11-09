import { useState, useEffect, useCallback } from 'react'
import { booksApi } from '../api/booksApi'
import { accessApi } from '../api/accessApi'
import { BookCard } from '../components/books/BookCard'
import { Pagination } from '../components/common/Pagination'
import { Alert } from '../components/common/Alert'
import { EmptyState } from '../components/common/EmptyState'
import { Spinner } from '../components/common/Spinner'
import { IconSearch, IconX, IconArrowUpDown } from '../components/common/Icons'
import { navigate } from '../app/router'

const PAGE_SIZE = 12

const SORT_OPTIONS = [
  { value: 'id,desc', label: 'Newest Arrivals' },
  { value: 'price,asc', label: 'Price: Low to High' },
  { value: 'price,desc', label: 'Price: High to Low' },
  { value: 'title,asc', label: 'Title: A to Z' },
  { value: 'title,desc', label: 'Title: Z to A' },
]

export function CatalogPage() {
  const [books, setBooks] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [appliedKeyword, setAppliedKeyword] = useState('')
  const [sortBy, setSortBy] = useState('id,desc')
  const [accessFilter, setAccessFilter] = useState('ALL') // 'ALL', 'FREE', 'PAID'
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')

  const fetchBooks = useCallback(async (currentKeyword, currentSort, currentAccess, currentPage) => {
    setLoading(true)
    setError('')
    try {
      const filters = {
        keyword: currentKeyword ? currentKeyword.trim() : undefined,
        accessType: currentAccess === 'ALL' ? undefined : currentAccess,
        active: true,
      }
      const params = {
        page: currentPage,
        size: PAGE_SIZE,
        sort: currentSort,
      }
      const result = await booksApi.searchBooks(filters, params)
      setBooks(result.content ?? [])
      setTotalPages(result.totalPages ?? 0)
      setTotalElements(result.totalElements ?? 0)
    } catch (err) {
      setError(err.message || 'Failed to load catalog.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchBooks(appliedKeyword, sortBy, accessFilter, page)
  }, [fetchBooks, appliedKeyword, sortBy, accessFilter, page])

  function handleSearch(e) {
    e.preventDefault()
    setPage(0)
    setAppliedKeyword(keyword)
  }

  function handleClearKeyword() {
    setKeyword('')
    setAppliedKeyword('')
    setPage(0)
  }

  function handleSortChange(e) {
    setSortBy(e.target.value)
    setPage(0)
  }

  function handleAccessChange(type) {
    setAccessFilter(type)
    setPage(0)
  }

  async function handleBookAction(book) {
    setActionError('')
    try {
      if (book.userBookAction === 'READ') {
        navigate(`/reader/${book.id}`)
        return
      }
      if (book.userBookAction === 'GET_FREE') {
        await accessApi.requestFreeBookAccess(book.id)
        await fetchBooks(appliedKeyword, sortBy, accessFilter, page)
        return
      }
      if (book.userBookAction === 'BUY') {
        navigate(`/books/${book.id}`)
        return
      }
      if (book.userBookAction === 'EXPIRED') {
        if (book.accessType === 'FREE') {
          await accessApi.requestFreeBookAccess(book.id)
          await fetchBooks(appliedKeyword, sortBy, accessFilter, page)
        } else {
          navigate(`/books/${book.id}`)
        }
        return
      }
    } catch (err) {
      setActionError(err.message || 'Could not process book action.')
    }
  }

  return (
    <div className="page-content fade-in">
      <div className="page-header catalog-hero-header">
        <div className="page-header-text">
          <h1>Browse Books</h1>
          <p>
            {totalElements > 0
              ? `Showing ${totalElements.toLocaleString()} books across all genres and authors`
              : 'Explore and discover books in our extensive digital library'}
          </p>
        </div>
      </div>

      {/* Modern Search & Sort Toolbar */}
      <div className="catalog-toolbar-card">
        <form className="catalog-search-form" onSubmit={handleSearch}>
          <div className="search-input-wrap">
            <span className="search-icon">
              <IconSearch size={18} />
            </span>
            <input
              type="text"
              className="input search-input-field"
              placeholder="Search by title, author, genre, or keyword…"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            {keyword && (
              <button
                type="button"
                className="search-clear-btn"
                onClick={handleClearKeyword}
                title="Clear search"
              >
                <IconX size={16} />
              </button>
            )}
          </div>
          <button type="submit" className="btn btn-primary search-submit-btn">
            Search
          </button>
        </form>

        <div className="catalog-controls-row">
          {/* Quick filter pills */}
          <div className="filter-pills-group">
            <button
              type="button"
              className={`filter-pill ${accessFilter === 'ALL' ? 'active' : ''}`}
              onClick={() => handleAccessChange('ALL')}
            >
              All Books
            </button>
            <button
              type="button"
              className={`filter-pill ${accessFilter === 'FREE' ? 'active' : ''}`}
              onClick={() => handleAccessChange('FREE')}
            >
              Free Access
            </button>
            <button
              type="button"
              className={`filter-pill ${accessFilter === 'PAID' ? 'active' : ''}`}
              onClick={() => handleAccessChange('PAID')}
            >
              Premium Access
            </button>
          </div>

          {/* Sort Selector */}
          <div className="sort-selector-wrap">
            <label htmlFor="catalog-sort" className="sort-label">
              <IconArrowUpDown size={15} style={{ marginRight: 6 }} />
              Sort by:
            </label>
            <select
              id="catalog-sort"
              className="select sort-select"
              value={sortBy}
              onChange={handleSortChange}
            >
              {SORT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {actionError && (
        <div className="mb-4">
          <Alert type="error">{actionError}</Alert>
        </div>
      )}

      {error && (
        <div className="mb-4">
          <Alert type="error">{error}</Alert>
        </div>
      )}

      {loading ? (
        <div className="loading-overlay show" style={{ minHeight: 300 }}>
          <Spinner size="lg" />
          <span>Finding books…</span>
        </div>
      ) : books.length === 0 ? (
        <EmptyState
          title="No books found"
          message={
            appliedKeyword
              ? `No books matched "${appliedKeyword}". Try searching with different keywords.`
              : 'There are no active books available under this filter.'
          }
          action={
            appliedKeyword || accessFilter !== 'ALL' ? (
              <button
                className="btn btn-outline"
                onClick={() => {
                  handleClearKeyword()
                  handleAccessChange('ALL')
                }}
              >
                Clear all filters
              </button>
            ) : null
          }
        />
      ) : (
        <>
          <div className="book-grid">
            {books.map((book) => (
              <BookCard
                key={book.id}
                book={book}
                onAction={handleBookAction}
              />
            ))}
          </div>
          <Pagination
            page={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  )
}
